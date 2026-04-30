package com.example.demo.modules.wip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.dispatch.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.dispatch.repository.SampleRepository;
import com.example.demo.modules.dispatch.repository.WIPbatchRepository;
import com.example.demo.modules.tempdb.repository.RequestRepository;
import com.example.demo.modules.tempdb.model.Equipment;
import com.example.demo.modules.tempdb.model.EquipmentStatusLogs;
import com.example.demo.modules.tempdb.model.Request;
import com.example.demo.modules.tempdb.model.Sample;
import com.example.demo.modules.tempdb.model.WIPbatch;
import com.example.demo.modules.wip.dto.WIPBatchDTO;

@Service
public class WipService {

    private final SampleRepository sampleRepository;
    private final WIPbatchRepository wipbatchRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final RequestRepository requestRepository;

    public WipService(SampleRepository sampleRepository,
                      WIPbatchRepository wipbatchRepository,
                      EquipmentStatusLogsRepository equipmentStatusLogsRepository,
                      RequestRepository requestRepository) {
        this.sampleRepository = sampleRepository;
        this.wipbatchRepository = wipbatchRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.requestRepository = requestRepository;
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
        updateEquipmentStatus(batch.getRecipe().getEquipment(), "BUSY");

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
        updateEquipmentStatus(batch.getRecipe().getEquipment(), "READY");

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

    private void checkAndUpdateRequestStatus(Request request) {
        // If all samples for this request are COMPLETED, mark request as DONE
        boolean allDone = sampleRepository.findByRequest_Id(request.getId()).stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));

        if (allDone) {
            request.setStatus("DONE");
            requestRepository.save(request);
        }
    }

    private WIPBatchDTO toWIPBatchDTO(WIPbatch batch) {
        WIPBatchDTO dto = new WIPBatchDTO();
        dto.setId(batch.getId());
        dto.setRecipeId(batch.getRecipe().getId());
        dto.setRecipeName(batch.getRecipe().getName());
        dto.setEquipmentId(batch.getRecipe().getEquipment().getId());
        dto.setEquipmentName(batch.getRecipe().getEquipment().getName());
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
