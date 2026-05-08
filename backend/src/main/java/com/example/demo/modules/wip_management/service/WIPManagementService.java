package com.example.demo.modules.wip_management.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;

@Service
public class WIPManagementService {

    private final SampleRepository sampleRepository;
    private final WIPbatchRepository wipbatchRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final RequestRepository requestRepository;
    private final com.example.demo.modules.notification.service.NotificationService notificationService;

    public WIPManagementService(SampleRepository sampleRepository,
                      WIPbatchRepository wipbatchRepository,
                      EquipmentStatusLogsRepository equipmentStatusLogsRepository,
                      RequestRepository requestRepository,
                      com.example.demo.modules.notification.service.NotificationService notificationService) {
        this.sampleRepository = sampleRepository;
        this.wipbatchRepository = wipbatchRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<WIPBatchDTO> getWIPBatches() {
        return wipbatchRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .map(this::toWIPBatchDTO)
                .toList();
    }

    @Transactional
    public WIPBatchDTO startBatch(Long id) {
        WIPbatch batch = wipbatchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!"QUEUED".equals(batch.getStatus())) {
            throw new RuntimeException("Only QUEUED batches can be started. Current status: " + batch.getStatus());
        }

        batch.setStatus("RUNNING");
        batch.setStartTime(LocalDateTime.now());
        WIPbatch savedBatch = wipbatchRepository.save(batch);

        // Update equipment status to BUSY
        updateEquipmentStatus(batch.getEquipment(), "BUSY");

        // Update samples status to RUNNING
        List<Sample> samples = sampleRepository.findByBatch_Id(id);
        for (Sample sample : samples) {
            sample.setStatus("RUNNING");
        }
        sampleRepository.saveAll(samples);

        // Check each affected request
        samples.stream()
                .map(Sample::getRequest)
                .distinct()
                .forEach(this::checkAndUpdateRequestStatus);

        notificationService.broadcast("REQUEST_UPDATED", "Batch started: " + id);
        return toWIPBatchDTO(savedBatch);
    }

    @Transactional
    public WIPBatchDTO finishBatch(Long id) {
        WIPbatch batch = wipbatchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!"RUNNING".equals(batch.getStatus())) {
            throw new RuntimeException("Only RUNNING batches can be finished. Current status: " + batch.getStatus());
        }

        batch.setStatus("FINISHED");
        batch.setEndTime(LocalDateTime.now());
        WIPbatch savedBatch = wipbatchRepository.save(batch);

        // Update equipment status back to READY
        updateEquipmentStatus(batch.getEquipment(), "READY");

        // Update samples status and check if request is done
        List<Sample> samples = sampleRepository.findByBatch_Id(id);
        for (Sample sample : samples) {
            sample.setStatus("COMPLETED");
        }
        sampleRepository.saveAll(samples);

        // Check each affected request
        samples.stream()
                .map(Sample::getRequest)
                .distinct()
                .forEach(this::checkAndUpdateRequestStatus);

        notificationService.broadcast("REQUEST_UPDATED", "Batch finished: " + id);
        return toWIPBatchDTO(savedBatch);
    }

    private void updateEquipmentStatus(Equipment equipment, String status) {
        // First end the previous log if any
        equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId())
                .ifPresent(log -> {
                    log.setEndTime(LocalDateTime.now());
                    equipmentStatusLogsRepository.save(log);
                });

        // Add new log
        EquipmentStatusLogs newLog = new EquipmentStatusLogs();
        newLog.setEquipment(equipment);
        newLog.setStatus(status);
        newLog.setStartTime(LocalDateTime.now());
        equipmentStatusLogsRepository.save(newLog);
    }

    public void checkAndUpdateRequestStatus(Request request) {
        List<Sample> allSamples = sampleRepository.findByRequest_Id(request.getId());
        
        boolean anyRunning = allSamples.stream()
                .anyMatch(s -> "RUNNING".equals(s.getStatus()));
        
        boolean allCompleted = allSamples.stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));
        
        boolean allAssignedOrMore = allSamples.stream()
                .allMatch(s -> "ASSIGNED".equals(s.getStatus()) || "RUNNING".equals(s.getStatus()) || "COMPLETED".equals(s.getStatus()));

        String newStatus = request.getStatus();

        if (allCompleted) {
            newStatus = "DONE";
        } else if (anyRunning) {
            newStatus = "PROCESSING";
        } else if (allAssignedOrMore) {
            newStatus = "DISPATCHED";
        }

        if (!newStatus.equals(request.getStatus())) {
            request.setStatus(newStatus);
            requestRepository.save(request);
        }
    }

    private WIPBatchDTO toWIPBatchDTO(WIPbatch batch) {
        WIPBatchDTO dto = new WIPBatchDTO();
        dto.setId(batch.getId());
        dto.setRecipeId(batch.getRecipe().getId());
        dto.setRecipeName(batch.getRecipe().getName());
        dto.setEquipmentId(batch.getEquipment().getId());
        dto.setEquipmentName(batch.getEquipment().getName());
        dto.setStatus(batch.getStatus());
        dto.setCreateTime(batch.getCreateTime());
        dto.setStartTime(batch.getStartTime());
        dto.setEndTime(batch.getEndTime());

        // Fill sample barcodes
        List<Sample> samples = sampleRepository.findByBatch_Id(batch.getId());
        dto.setSampleBarcodes(samples.stream().map(Sample::getBarcode).toList());

        return dto;
    }
}
