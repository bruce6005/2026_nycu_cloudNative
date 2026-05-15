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
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

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

    @Transactional
    public List<WIPBatchDTO> getWIPBatches() {
        autoResolveExpiredRunningBatches();

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

        LocalDateTime now = LocalDateTime.now();

        int randomSeconds = ThreadLocalRandom.current().nextInt(5, 10);
        boolean willCrash = ThreadLocalRandom.current().nextInt(100) < 25;//25 percent crash

        batch.setStatus(willCrash ? "RUNNING_CRASH" : "RUNNING");
        batch.setStartTime(now);
        batch.setEstimatedEndTime(now.plusSeconds(randomSeconds));

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
            throw new RuntimeException("Only RUNNING batches can be manually finished. Current status: " + batch.getStatus());
        }

        WIPbatch savedBatch = finishRunningBatch(batch);

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

        boolean anyFailed = allSamples.stream()
                .anyMatch(s -> "FAILED".equals(s.getStatus()));

        boolean anyRunning = allSamples.stream()
                .anyMatch(s -> "RUNNING".equals(s.getStatus())
                        || "RUNNING_CRASH".equals(s.getStatus()));

        boolean allCompleted = allSamples.stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));

        boolean allAssignedOrMore = allSamples.stream()
                .allMatch(s -> "ASSIGNED".equals(s.getStatus())
                        || "RUNNING".equals(s.getStatus())
                        || "RUNNING_CRASH".equals(s.getStatus())
                        || "COMPLETED".equals(s.getStatus())
                        || "FAILED".equals(s.getStatus()));

        String newStatus = request.getStatus();

        if (anyFailed) {
            newStatus = "FAILED";
        } else if (allCompleted) {
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
        dto.setEstimatedEndTime(batch.getEstimatedEndTime());
        dto.setProgressPercent(calculateProgressPercent(batch));
        dto.setRemainingSeconds(calculateRemainingSeconds(batch));

        // Fill sample barcodes
        List<Sample> samples = sampleRepository.findByBatch_Id(batch.getId());
        dto.setSampleBarcodes(samples.stream().map(Sample::getBarcode).toList());

        return dto;
    }
    private Integer calculateProgressPercent(WIPbatch batch) {
        if ("FINISHED".equals(batch.getStatus()) || "FAILED".equals(batch.getStatus())) {
            return 100;
        }

        if (!isRunningStatus(batch.getStatus())
                || batch.getStartTime() == null
                || batch.getEstimatedEndTime() == null) {
            return 0;
        }

        long totalSeconds = Duration.between(batch.getStartTime(), batch.getEstimatedEndTime()).toSeconds();
        long elapsedSeconds = Duration.between(batch.getStartTime(), LocalDateTime.now()).toSeconds();

        if (totalSeconds <= 0) {
            return 100;
        }

        int progress = (int) Math.floor(elapsedSeconds * 100.0 / totalSeconds);

        if (progress < 0) {
            return 0;
        }

        if (progress > 100) {
            return 100;
        }

        return progress;
    }

    private Long calculateRemainingSeconds(WIPbatch batch) {
        if (!isRunningStatus(batch.getStatus()) || batch.getEstimatedEndTime() == null) {
            return 0L;
        }

        long remaining = Duration.between(LocalDateTime.now(), batch.getEstimatedEndTime()).toSeconds();

        return Math.max(remaining, 0L);
    }

    private boolean isRunningStatus(String status) {
        return "RUNNING".equals(status) || "RUNNING_CRASH".equals(status);
    }

    private WIPbatch finishRunningBatch(WIPbatch batch) {
        if (!"RUNNING".equals(batch.getStatus())) {
            throw new RuntimeException("Only RUNNING batches can be finished. Current status: " + batch.getStatus());
        }

        batch.setStatus("FINISHED");
        batch.setEndTime(LocalDateTime.now());

        WIPbatch savedBatch = wipbatchRepository.save(batch);

        updateEquipmentStatus(savedBatch.getEquipment(), "READY");

        List<Sample> samples = sampleRepository.findByBatch_Id(savedBatch.getId());

        for (Sample sample : samples) {
            sample.setStatus("COMPLETED");
        }

        sampleRepository.saveAll(samples);

        samples.stream()
                .map(Sample::getRequest)
                .distinct()
                .forEach(this::checkAndUpdateRequestStatus);

        return savedBatch;
    }

    private void createAlarmForBatchFailure(WIPbatch batch) {
        System.out.println("[ALARM] Batch failed. batchId="
                + batch.getId()
                + ", equipmentId="
                + batch.getEquipment().getId()
                + ", equipmentName="
                + batch.getEquipment().getName());
    }
    private WIPbatch failRunningBatch(WIPbatch batch) {
        if (!"RUNNING_CRASH".equals(batch.getStatus())) {
            throw new RuntimeException("Only RUNNING_CRASH batches can fail. Current status: " + batch.getStatus());
        }

        batch.setStatus("FAILED");
        batch.setEndTime(LocalDateTime.now());

        WIPbatch savedBatch = wipbatchRepository.save(batch);

        updateEquipmentStatus(savedBatch.getEquipment(), "ERROR");

        List<Sample> samples = sampleRepository.findByBatch_Id(savedBatch.getId());

        for (Sample sample : samples) {
            sample.setStatus("FAILED");
        }

        sampleRepository.saveAll(samples);

        samples.stream()
                .map(Sample::getRequest)
                .distinct()
                .forEach(this::checkAndUpdateRequestStatus);

        createAlarmForBatchFailure(savedBatch);

        return savedBatch;
    }
    private void autoResolveExpiredRunningBatches() {
        LocalDateTime now = LocalDateTime.now();

        List<WIPbatch> runningBatches = wipbatchRepository.findByStatusIn(
                Arrays.asList("RUNNING", "RUNNING_CRASH"));

        for (WIPbatch batch : runningBatches) {
            LocalDateTime estimatedEndTime = batch.getEstimatedEndTime();

            if (estimatedEndTime != null && !estimatedEndTime.isAfter(now)) {
                if ("RUNNING_CRASH".equals(batch.getStatus())) {
                    failRunningBatch(batch);
                    notificationService.broadcast("REQUEST_UPDATED", "Batch failed: " + batch.getId());
                } else {
                    finishRunningBatch(batch);
                    notificationService.broadcast("REQUEST_UPDATED", "Batch finished: " + batch.getId());
                }
            }
        }
    }
}
