package com.example.demo.modules.manager_dashboard.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class ManagerDashboardService {

    private final RequestRepository requestRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final TestRecordsRepository testRecordsRepository;

    public ManagerDashboardService(
            RequestRepository requestRepository,
            EquipmentRepository equipmentRepository,
            EquipmentStatusLogsRepository equipmentStatusLogsRepository,
            TestRecordsRepository testRecordsRepository) {
        this.requestRepository = requestRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.testRecordsRepository = testRecordsRepository;
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

    @Transactional(readOnly = true)
    public List<EquipmentUsageDTO> getEquipmentUsage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusHours(24);
        long totalMinutes = Duration.between(windowStart, now).toMinutes();

        return equipmentRepository.findAll().stream()
                .map(equipment -> toEquipmentUsageDTO(equipment, windowStart, now, totalMinutes))
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

    private EquipmentUsageDTO toEquipmentUsageDTO(
            Equipment equipment,
            LocalDateTime windowStart,
            LocalDateTime now,
            long totalMinutes) {
        List<EquipmentStatusLogs> logs = equipmentStatusLogsRepository.findByEquipmentId(equipment.getId());

        long runningMinutes = logs.stream()
                .filter(log -> "RUNNING".equalsIgnoreCase(log.getStatus()))
                .mapToLong(log -> calculateOverlapMinutes(log, windowStart, now))
                .sum();

        double usageRate = totalMinutes == 0
                ? 0.0
                : Math.round((runningMinutes * 10000.0 / totalMinutes)) / 100.0;

        String currentStatus = equipmentStatusLogsRepository
                .findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId())
                .or(() -> equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipment.getId()))
                .map(EquipmentStatusLogs::getStatus)
                .orElse(null);

        String equipmentType = equipment.getEquipmentTypeSchema() == null
                ? "-"
                : equipment.getEquipmentTypeSchema().getEquipmentType();

        return new EquipmentUsageDTO(
                equipment.getId(),
                equipment.getName(),
                equipmentType,
                runningMinutes,
                totalMinutes,
                usageRate,
                currentStatus);
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
}