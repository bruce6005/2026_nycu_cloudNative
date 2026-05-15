package com.example.demo.modules.manager_dashboard.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;

import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.request.repository.SampleRepository;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.manager_dashboard.dto.EquipmentUsageDTO;
import com.example.demo.modules.manager_dashboard.dto.RequestStatsDTO;
import com.example.demo.modules.manager_dashboard.dto.TestRecordLogDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.wip_builder.model.TestRecords;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.repository.TestRecordsRepository;
import com.example.demo.modules.notification.service.NotificationService;

@Service
public class ManagerDashboardService {

    private final RequestRepository requestRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final TestRecordsRepository testRecordsRepository;
    private final WIPbatchRepository wipbatchRepository;
    private final NotificationService notificationService;
    private final SampleRepository sampleRepository;

    public ManagerDashboardService(
            RequestRepository requestRepository,
            EquipmentRepository equipmentRepository,
            EquipmentStatusLogsRepository equipmentStatusLogsRepository,
            TestRecordsRepository testRecordsRepository,
            WIPbatchRepository wipbatchRepository,
            NotificationService notificationService,
            SampleRepository sampleRepository) {
        this.requestRepository = requestRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.testRecordsRepository = testRecordsRepository;
        this.wipbatchRepository = wipbatchRepository;
        this.notificationService = notificationService;
        this.sampleRepository = sampleRepository;
    }

    @Transactional(readOnly = true)
    public RequestStatsDTO getRequestStats() {
        List<Request> requests = requestRepository.findAll();

        long total = requests.size();
        long pending = countByStatus(requests, "PENDING", "SUBMITTED");
        long approved = countByStatus(requests, "APPROVED", "ACCEPTED");
        long dispatched = countByStatus(requests, "DISPATCHED", "PROCESSING");
        long completed = countByStatus(requests, "DONE", "COMPLETED");
        long rejected = countByStatus(requests, "REJECTED");

        return new RequestStatsDTO(
                total,
                pending,
                approved,
                dispatched,
                completed,
                rejected);
    }
    private void autoResolveExpiredRunningBatches() {
        LocalDateTime now = LocalDateTime.now();

        List<WIPbatch> runningBatches = wipbatchRepository.findByStatusIn(
                List.of("RUNNING", "RUNNING_CRASH")
        );

        for (WIPbatch batch : runningBatches) {
            LocalDateTime estimatedEndTime = batch.getEstimatedEndTime();

            if (estimatedEndTime != null && !estimatedEndTime.isAfter(now)) {
                if ("RUNNING_CRASH".equals(batch.getStatus())) {
                    failRunningBatch(batch);
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                notificationService.broadcast("REQUEST_UPDATED", "Batch failed: " + batch.getId());
                            }
                        });
                    } else {
                        notificationService.broadcast("REQUEST_UPDATED", "Batch failed: " + batch.getId());
                    }
                } else {
                    finishRunningBatch(batch);
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                notificationService.broadcast("REQUEST_UPDATED", "Batch finished: " + batch.getId());
                            }
                        });
                    } else {
                        notificationService.broadcast("REQUEST_UPDATED", "Batch finished: " + batch.getId());
                    }
                }
            }
        }
    }
    @Transactional
    public List<EquipmentUsageDTO> getEquipmentUsage() {
        autoResolveExpiredRunningBatches();

        List<WIPbatch> allBatches = wipbatchRepository.findAll();

        long totalUsageCount = allBatches.stream()
                .filter(batch -> batch.getEquipment() != null)
                .count();

        return equipmentRepository.findAll().stream()
                .map(equipment -> toEquipmentUsageDTO(equipment, allBatches, totalUsageCount))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<TestRecordLogDTO> getTestRecordLogs() {
        return testRecordsRepository.findTop50ByOrderByStartTimeDesc().stream()
                .map(this::toTestRecordLogDTO)
                .toList();
    }

    private long countByStatus(List<Request> requests, String... statuses) {
        return requests.stream()
                .filter(request -> matchesStatus(request.getStatus(), statuses))
                .count();
    }

    private boolean matchesStatus(String actualStatus, String... expectedStatuses) {
        if (actualStatus == null) {
            return false;
        }

        String normalizedActual = actualStatus.trim().toUpperCase();

        for (String expectedStatus : expectedStatuses) {
            if (normalizedActual.equals(expectedStatus)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBusyStatus(String status) {
        if (status == null) {
            return false;
        }

        String normalized = status.trim().toUpperCase();

        return "BUSY".equals(normalized)
                || "RUNNING".equals(normalized)
                || "PROCESSING".equals(normalized);
    }

    private EquipmentUsageDTO toEquipmentUsageDTO(
            Equipment equipment,
            List<WIPbatch> allBatches,
            long totalUsageCount) {

        List<WIPbatch> equipmentBatches = allBatches.stream()
                .filter(batch -> batch.getEquipment() != null)
                .filter(batch -> batch.getEquipment().getId().equals(equipment.getId()))
                .toList();

        long usageCount = equipmentBatches.size();

        double usageRate = totalUsageCount == 0
                ? 0.0
                : Math.round(usageCount * 10000.0 / totalUsageCount) / 100.0;

        long successCount = equipmentBatches.stream()
                .filter(batch -> "FINISHED".equalsIgnoreCase(batch.getStatus()))
                .count();

        long failedCount = equipmentBatches.stream()
                .filter(batch -> "FAILED".equalsIgnoreCase(batch.getStatus()))
                .count();

        double failureRate = usageCount == 0
                ? 0.0
                : Math.round(failedCount * 10000.0 / usageCount) / 100.0;

        long averageRunSeconds = calculateAverageRunSeconds(equipmentBatches);

        WIPbatch activeBatch = equipmentBatches.stream()
                .filter(batch -> "RUNNING".equals(batch.getStatus())
                        || "RUNNING_CRASH".equals(batch.getStatus()))
                .findFirst()
                .orElse(null);

        Long activeBatchId = activeBatch == null ? null : activeBatch.getId();
        String activeBatchStatus = activeBatch == null ? null : activeBatch.getStatus();
        Integer activeProgressPercent = activeBatch == null ? null : calculateBatchProgressPercent(activeBatch);
        Long remainingSeconds = activeBatch == null ? null : calculateBatchRemainingSeconds(activeBatch);

        String currentStatus = resolveCurrentEquipmentStatus(equipment.getId());

        String equipmentType = equipment.getEquipmentTypeSchema() == null
                ? "-"
                : equipment.getEquipmentTypeSchema().getEquipmentType();

        return new EquipmentUsageDTO(
                equipment.getId(),
                equipment.getName(),
                equipmentType,
                usageCount,
                totalUsageCount,
                usageRate,
                averageRunSeconds,
                successCount,
                failedCount,
                failureRate,
                currentStatus,
                activeBatchId,
                activeBatchStatus,
                activeProgressPercent,
                remainingSeconds
        );
    }

    private long calculateOverlapMinutes(
            EquipmentStatusLogs log,
            LocalDateTime windowStart,
            LocalDateTime now) {
        LocalDateTime logStart = log.getStartTime();
        LocalDateTime logEnd = log.getEndTime() == null ? now : log.getEndTime();

        LocalDateTime effectiveStart = logStart.isBefore(windowStart) ? windowStart : logStart;
        LocalDateTime effectiveEnd = logEnd.isAfter(now) ? now : logEnd;

        if (!effectiveEnd.isAfter(effectiveStart)) {
            return 0;
        }

        return Duration.between(effectiveStart, effectiveEnd).toMinutes();
    }

    private TestRecordLogDTO toTestRecordLogDTO(TestRecords record) {
        Long batchId = record.getBatch() == null ? null : record.getBatch().getId();

        Long equipmentId = record.getEquipment() == null ? null : record.getEquipment().getId();
        String equipmentName = record.getEquipment() == null ? "-" : record.getEquipment().getName();

        Long operatorId = record.getOperator() == null ? null : record.getOperator().getId();
        String operatorName = record.getOperator() == null ? "-" : record.getOperator().getName();

        return new TestRecordLogDTO(
                record.getId(),
                batchId,
                equipmentId,
                equipmentName,
                operatorId,
                operatorName,
                record.getResultStatus(),
                record.getResultData(),
                record.getStartTime(),
                record.getEndTime());
    }
    // helper
    private long calculateAverageRunSeconds(List<WIPbatch> batches) {
        List<WIPbatch> completedBatches = batches.stream()
                .filter(batch -> batch.getStartTime() != null && batch.getEndTime() != null)
                .toList();

        if (completedBatches.isEmpty()) {
            return 0L;
        }

        long totalSeconds = completedBatches.stream()
                .mapToLong(batch -> Duration.between(batch.getStartTime(), batch.getEndTime()).toSeconds())
                .sum();

        return totalSeconds / completedBatches.size();
    }

    private Integer calculateBatchProgressPercent(WIPbatch batch) {
        if ("FINISHED".equals(batch.getStatus()) || "FAILED".equals(batch.getStatus())) {
            return 100;
        }

        if (!isRunningBatchStatus(batch.getStatus())
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

    private Long calculateBatchRemainingSeconds(WIPbatch batch) {
        if (!isRunningBatchStatus(batch.getStatus()) || batch.getEstimatedEndTime() == null) {
            return 0L;
        }

        long remaining = Duration.between(LocalDateTime.now(), batch.getEstimatedEndTime()).toSeconds();

        return Math.max(remaining, 0L);
    }

    private boolean isRunningBatchStatus(String status) {
        return "RUNNING".equals(status) || "RUNNING_CRASH".equals(status);
    }

    private String resolveCurrentEquipmentStatus(Long equipmentId) {
        return equipmentStatusLogsRepository
                .findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipmentId)
                .or(() -> equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipmentId))
                .map(log -> log.getStatus())
                .orElse("UNKNOWN");
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
    private void checkAndUpdateRequestStatus(Request request) {
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

    private void updateEquipmentStatus(Equipment equipment, String status) {
        equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId())
                .ifPresent(log -> {
                    log.setEndTime(LocalDateTime.now());
                    equipmentStatusLogsRepository.save(log);
                });

        EquipmentStatusLogs newLog = new EquipmentStatusLogs();
        newLog.setEquipment(equipment);
        newLog.setStatus(status);
        newLog.setStartTime(LocalDateTime.now());

        equipmentStatusLogsRepository.save(newLog);
    }

    private void createAlarmForBatchFailure(WIPbatch batch) {
        System.out.println("[ALARM] Batch failed. batchId="
                + batch.getId()
                + ", equipmentId="
                + batch.getEquipment().getId()
                + ", equipmentName="
                + batch.getEquipment().getName());
    }
}