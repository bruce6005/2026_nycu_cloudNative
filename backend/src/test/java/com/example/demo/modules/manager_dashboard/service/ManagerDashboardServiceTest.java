package com.example.demo.modules.manager_dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.manager_dashboard.dto.EquipmentUsageDTO;
import com.example.demo.modules.manager_dashboard.dto.RequestStatsDTO;
import com.example.demo.modules.manager_dashboard.dto.TestRecordLogDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.wip_builder.model.TestRecords;
import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.repository.TestRecordsRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;

@ExtendWith(MockitoExtension.class)
class ManagerDashboardServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EquipmentStatusLogsRepository equipmentStatusLogsRepository;

    @Mock
    private TestRecordsRepository testRecordsRepository;

    @Mock
    private WIPbatchRepository wipbatchRepository;

    @InjectMocks
    private ManagerDashboardService managerDashboardService;

    private EquipmentTypeSchema buildSchema(Long id, String type) {
        EquipmentTypeSchema schema = new EquipmentTypeSchema();
        schema.setId(id);
        schema.setEquipmentType(type);
        schema.setParameterSchema("{}");
        return schema;
    }

    private Equipment buildEquipment(Long id, String name, Long schemaId) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName(name);
        equipment.setEquipmentTypeSchema(buildSchema(schemaId, "TYPE" + schemaId));
        return equipment;
    }

    private Request buildRequest(Long id, String status) {
        Request request = new Request();
        request.setId(id);
        request.setStatus(status);
        return request;
    }

    private EquipmentStatusLogs buildStatusLog(
            Equipment equipment,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        EquipmentStatusLogs log = new EquipmentStatusLogs();
        log.setEquipment(equipment);
        log.setStatus(status);
        log.setStartTime(startTime);
        log.setEndTime(endTime);
        return log;
    }

    private WIPbatch buildBatch(Long id, Equipment equipment, String status) {
        WIPbatch batch = new WIPbatch();
        batch.setId(id);
        batch.setEquipment(equipment);
        batch.setStatus(status);
        return batch;
    }

    private TestRecords buildTestRecord(
            Long id,
            WIPbatch batch,
            Equipment equipment,
            Long operatorId,
            String status) {
        TestRecords record = new TestRecords();
        record.setId(id);
        record.setBatch(batch);
        record.setEquipment(equipment);

        if (operatorId != null) {
            var user = new com.example.demo.modules.auth.model.User();
            user.setId(operatorId);
            user.setName("op" + operatorId);
            record.setOperator(user);
        }

        record.setResultStatus(status);
        record.setResultData("{}");
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(null);

        return record;
    }

    @Test
    @DisplayName("getRequestStats() should count request statuses correctly")
    void getRequestStats_countsCorrectly() {
        Request r1 = buildRequest(1L, "PENDING");
        Request r2 = buildRequest(2L, "SUBMITTED");
        Request r3 = buildRequest(3L, "APPROVED");
        Request r4 = buildRequest(4L, "ACCEPTED");
        Request r5 = buildRequest(5L, "DISPATCHED");
        Request r6 = buildRequest(6L, "PROCESSING");
        Request r7 = buildRequest(7L, "DONE");
        Request r8 = buildRequest(8L, "COMPLETED");
        Request r9 = buildRequest(9L, "REJECTED");

        when(requestRepository.findAll()).thenReturn(List.of(
                r1, r2, r3, r4, r5, r6, r7, r8, r9
        ));

        RequestStatsDTO stats = managerDashboardService.getRequestStats();

        assertEquals(9, stats.getTotalRequests());
        assertEquals(2, stats.getPendingRequests());
        assertEquals(2, stats.getApprovedRequests());
        assertEquals(2, stats.getDispatchedRequests());
        assertEquals(2, stats.getCompletedRequests());
        assertEquals(1, stats.getRejectedRequests());
    }

    @Test
    @DisplayName("getEquipmentUsage() should return zero usage when there are no running logs")
    void getEquipmentUsage_noRunningLogs_shouldReturnZeroUsage() {
        Equipment equipment = buildEquipment(1L, "EQ-1", 100L);

        EquipmentStatusLogs currentLog = buildStatusLog(
                equipment,
                "FREE",
                LocalDateTime.now().minusHours(1),
                null
        );

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.of(currentLog));
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of());

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        assertEquals(1, result.size());

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(equipment.getId(), dto.getEquipmentId());
        assertEquals("EQ-1", dto.getEquipmentName());
        assertEquals("TYPE100", dto.getEquipmentType());
        assertEquals(0L, dto.getRunningMinutes());
        assertEquals(0.0, dto.getUsageRate());
        assertEquals("FREE", dto.getCurrentStatus());
        assertEquals(0L, dto.getUsageCount());
        assertEquals(0L, dto.getTotalUsageCount());
        assertEquals(0L, dto.getAverageRunSeconds());
        assertEquals(0L, dto.getSuccessCount());
        assertEquals(0L, dto.getFailedCount());
        assertEquals(0.0, dto.getFailureRate());
        assertEquals(null, dto.getActiveBatchId());
        assertEquals(null, dto.getActiveBatchStatus());
        assertEquals(0.0, dto.getActiveProgressPercent());
        assertEquals(0L, dto.getRemainingSeconds());
    }

    @Test
    @DisplayName("getEquipmentUsage() should calculate usage rate from running status logs")
    void getEquipmentUsage_usageRateCalculation() {
        Equipment equipment = buildEquipment(1L, "EQ-A", 100L);

        LocalDateTime now = LocalDateTime.now();

        EquipmentStatusLogs runningLog = buildStatusLog(
                equipment,
                "RUNNING",
                now.minusMinutes(432),
                now
        );

        EquipmentStatusLogs currentLog = buildStatusLog(
                equipment,
                "RUNNING",
                now.minusMinutes(10),
                null
        );

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of(runningLog));
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.of(currentLog));
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of());

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(432L, dto.getRunningMinutes());
        assertEquals(1440L, dto.getTotalMinutes());
        assertEquals(30.0, dto.getUsageRate());
        assertEquals("RUNNING", dto.getCurrentStatus());
    }

    @Test
    @DisplayName("getEquipmentUsage() should count BUSY logs as usage")
    void getEquipmentUsage_busyLog_shouldCountAsUsage() {
        Equipment equipment = buildEquipment(1L, "EQ-A", 100L);

        LocalDateTime now = LocalDateTime.now();

        EquipmentStatusLogs busyLog = buildStatusLog(
                equipment,
                "BUSY",
                now.minusMinutes(144),
                now
        );

        EquipmentStatusLogs currentLog = buildStatusLog(
                equipment,
                "BUSY",
                now.minusMinutes(5),
                null
        );

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of(busyLog));
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.of(currentLog));
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of());

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(144L, dto.getRunningMinutes());
        assertEquals(10.0, dto.getUsageRate());
        assertEquals("BUSY", dto.getCurrentStatus());
    }

    @Test
    @DisplayName("getEquipmentUsage() should calculate batch counts and failure rate")
    void getEquipmentUsage_batchCountsAndFailureRate() {
        Equipment equipment = buildEquipment(1L, "EQ-A", 100L);

        WIPbatch finishedBatch = buildBatch(1L, equipment, "FINISHED");
        WIPbatch failedBatch = buildBatch(2L, equipment, "FAILED");
        WIPbatch queuedBatch = buildBatch(3L, equipment, "QUEUED");

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of(finishedBatch, failedBatch, queuedBatch));

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(3L, dto.getTotalUsageCount());
        assertEquals(2L, dto.getUsageCount());
        assertEquals(1L, dto.getSuccessCount());
        assertEquals(1L, dto.getFailedCount());
        assertEquals(50.0, dto.getFailureRate());
    }

    @Test
    @DisplayName("getEquipmentUsage() should calculate average run seconds")
    void getEquipmentUsage_averageRunSeconds() {
        Equipment equipment = buildEquipment(1L, "EQ-A", 100L);

        LocalDateTime base = LocalDateTime.now();

        WIPbatch batch1 = buildBatch(1L, equipment, "FINISHED");
        batch1.setStartTime(base.minusMinutes(10));
        batch1.setEndTime(base.minusMinutes(5));

        WIPbatch batch2 = buildBatch(2L, equipment, "FINISHED");
        batch2.setStartTime(base.minusMinutes(20));
        batch2.setEndTime(base.minusMinutes(10));

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of(batch1, batch2));

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(450L, dto.getAverageRunSeconds());
    }

    @Test
    @DisplayName("getEquipmentUsage() should include active running batch information")
    void getEquipmentUsage_activeBatchInfo() {
        Equipment equipment = buildEquipment(1L, "EQ-A", 100L);

        LocalDateTime now = LocalDateTime.now();

        WIPbatch runningBatch = buildBatch(99L, equipment, "RUNNING");
        runningBatch.setStartTime(now.minusMinutes(30));
        runningBatch.setEstimatedEndTime(now.plusMinutes(30));

        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentStatusLogsRepository.findByEquipmentId(equipment.getId()))
                .thenReturn(List.of());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipment.getId()))
                .thenReturn(Optional.empty());
        when(wipbatchRepository.findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(equipment.getId(), "RUNNING"))
                .thenReturn(Optional.of(runningBatch));
        when(wipbatchRepository.findByEquipment_Id(equipment.getId()))
                .thenReturn(List.of(runningBatch));

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO dto = result.get(0);

        assertEquals(99L, dto.getActiveBatchId());
        assertEquals("RUNNING", dto.getActiveBatchStatus());
        assertEquals(50.0, dto.getActiveProgressPercent());
        assertEquals(1800L, dto.getRemainingSeconds());
    }

    @Test
    @DisplayName("getTestRecordLogs() should return mapped DTO list")
    void getTestRecordLogs_returnsMappedDTO() {
        Equipment equipment = buildEquipment(1L, "EQ-1", 100L);

        WIPbatch batch = new WIPbatch();
        batch.setId(500L);

        TestRecords record = buildTestRecord(1L, batch, equipment, 10L, "QUEUED");

        when(testRecordsRepository.findTop50ByOrderByStartTimeDesc())
                .thenReturn(List.of(record));

        List<TestRecordLogDTO> logs = managerDashboardService.getTestRecordLogs();

        assertEquals(1, logs.size());

        TestRecordLogDTO dto = logs.get(0);

        assertEquals(record.getId(), dto.getId());
        assertEquals(batch.getId(), dto.getBatchId());
        assertEquals(equipment.getId(), dto.getEquipmentId());
        assertEquals("EQ-1", dto.getEquipmentName());
        assertEquals(10L, dto.getOperatorId());
        assertEquals("op10", dto.getOperatorName());
        assertEquals("QUEUED", dto.getResultStatus());
        assertEquals("{}", dto.getResultData());
        assertEquals(record.getStartTime(), dto.getStartTime());
        assertEquals(record.getEndTime(), dto.getEndTime());
    }
}