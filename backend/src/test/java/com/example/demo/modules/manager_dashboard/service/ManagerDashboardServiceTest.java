package com.example.demo.modules.manager_dashboard.service;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.manager_dashboard.dto.EquipmentUsageDTO;
import com.example.demo.modules.manager_dashboard.dto.RequestStatsDTO;
import com.example.demo.modules.manager_dashboard.dto.TestRecordLogDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.wip_builder.model.TestRecords;
import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.repository.TestRecordsRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.notification.service.NotificationService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private NotificationService notificationService;
    @Mock
    private SampleRepository sampleRepository;

    @InjectMocks
    private ManagerDashboardService managerDashboardService;

    // helpers
    private EquipmentTypeSchema buildSchema(Long id, String type) {
        EquipmentTypeSchema s = new EquipmentTypeSchema();
        s.setId(id);
        s.setEquipmentType(type);
        s.setParameterSchema("{}");
        return s;
    }

    private Equipment buildEquipment(Long id, String name, Long schemaId) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setName(name);
        e.setEquipmentTypeSchema(buildSchema(schemaId, "TYPE" + schemaId));
        return e;
    }

    private Request buildRequest(Long id, String status) {
        Request r = new Request();
        r.setId(id);
        r.setStatus(status);
        return r;
    }

    private TestRecords buildTestRecord(Long id, WIPbatch batch, Equipment equipment, Long operatorId, String status) {
        TestRecords t = new TestRecords();
        t.setId(id);
        t.setBatch(batch);
        t.setEquipment(equipment);
        if (operatorId != null) {
            var user = new com.example.demo.modules.auth.model.User();
            user.setId(operatorId);
            user.setName("op" + operatorId);
            t.setOperator(user);
        }
        t.setResultStatus(status);
        t.setResultData("{}");
        t.setStartTime(LocalDateTime.now());
        t.setEndTime(null);
        return t;
    }

    // -------------------------------------------------------
    // getRequestStats()
    // -------------------------------------------------------

    @Test
    @DisplayName("getRequestStats() - 應正確計算各狀態數量")
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

        when(requestRepository.findAll()).thenReturn(List.of(r1,r2,r3,r4,r5,r6,r7,r8,r9));

        RequestStatsDTO stats = managerDashboardService.getRequestStats();

        assertEquals(9, stats.getTotalRequests());
        assertEquals(2, stats.getPendingRequests());
        assertEquals(2, stats.getApprovedRequests());
        assertEquals(2, stats.getDispatchedRequests());
        assertEquals(2, stats.getCompletedRequests());
        assertEquals(1, stats.getRejectedRequests());
    }

    // -------------------------------------------------------
    // getEquipmentUsage()
    // -------------------------------------------------------

    @Test
    @DisplayName("getEquipmentUsage() - 無批次時回傳設備使用率為 0")
    void getEquipmentUsage_noBatches_shouldReturnZeroUsage() {
        Equipment eq = buildEquipment(1L, "EQ-1", 100L);

        when(wipbatchRepository.findAll()).thenReturn(List.of());
        when(equipmentRepository.findAll()).thenReturn(List.of(eq));

        // 模擬目前設備狀態
        com.example.demo.modules.wip_builder.model.EquipmentStatusLogs log = new com.example.demo.modules.wip_builder.model.EquipmentStatusLogs();
        log.setStatus("FREE");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(eq.getId()))
                .thenReturn(Optional.of(log));

        List<EquipmentUsageDTO> result = managerDashboardService.getEquipmentUsage();

        assertEquals(1, result.size());
        EquipmentUsageDTO dto = result.get(0);
        assertEquals(0L, dto.getUsageCount());
        assertEquals(0L, dto.getTotalUsageCount());
        assertEquals(0.0, dto.getUsageRate());
        assertEquals("FREE", dto.getCurrentStatus());
    }

    // -------------------------------------------------------
    // getTestRecordLogs()
    // -------------------------------------------------------

    @Test
    @DisplayName("getTestRecordLogs() - 應回傳轉換後的 DTO 列表")
    void getTestRecordLogs_returnsMappedDTO() {
        Equipment eq = buildEquipment(1L, "EQ-1", 100L);
        WIPbatch batch = new WIPbatch();
        batch.setId(500L);

        TestRecords tr1 = buildTestRecord(1L, batch, eq, 10L, "QUEUED");

        when(testRecordsRepository.findTop50ByOrderByStartTimeDesc()).thenReturn(List.of(tr1));

        List<TestRecordLogDTO> logs = managerDashboardService.getTestRecordLogs();

        assertEquals(1, logs.size());
        TestRecordLogDTO dto = logs.get(0);
        assertEquals(tr1.getId(), dto.getId());
        assertEquals(batch.getId(), dto.getBatchId());
        assertEquals(eq.getId(), dto.getEquipmentId());
        assertEquals("QUEUED", dto.getResultStatus());
    }

    // -------------------------------------------------------
    // checkAndUpdateRequestStatus() - parameterized
    // -------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideRequestStatusCases")
    @DisplayName("checkAndUpdateRequestStatus() - 各種樣本狀態應對應到正確 Request 狀態")
    void checkAndUpdateRequestStatus_variousScenarios(List<String> sampleStatuses, String expectedRequestStatus) throws Exception {
        Request req = buildRequest(999L, "PENDING"); // initial status

        List<Sample> samples = createSamplesForStatuses(req, sampleStatuses);

        when(sampleRepository.findByRequest_Id(req.getId())).thenReturn(samples);

        // invoke private method via reflection
        Method m = ManagerDashboardService.class.getDeclaredMethod("checkAndUpdateRequestStatus", Request.class);
        m.setAccessible(true);
        m.invoke(managerDashboardService, req);

        assertEquals(expectedRequestStatus, req.getStatus());
        // should save when status changed from initial
        if (!"PENDING".equals(expectedRequestStatus)) {
            verify(requestRepository, times(1)).save(req);
        }
    }

    private static Stream<Arguments> provideRequestStatusCases() {
        return Stream.of(
                // 1-1 Any Failed: 4 COMPLETED, 1 FAILED -> FAILED
                Arguments.of(List.of("COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "FAILED"), "FAILED"),
                // 1-2 All Completed -> DONE
                Arguments.of(List.of("COMPLETED", "COMPLETED", "COMPLETED"), "DONE"),
                // 1-3 Processing: 1 COMPLETED, 1 RUNNING, 1 ASSIGNED -> PROCESSING
                Arguments.of(List.of("COMPLETED", "RUNNING", "ASSIGNED"), "PROCESSING"),
                // 1-4 Dispatched: all ASSIGNED -> DISPATCHED
                Arguments.of(List.of("ASSIGNED", "ASSIGNED"), "DISPATCHED")
        );
    }

    private List<Sample> createSamplesForStatuses(Request req, List<String> statuses) {
        AtomicLong id = new AtomicLong(1000L);
        return statuses.stream().map(s -> {
            Sample samp = new Sample();
            samp.setId(id.getAndIncrement());
            samp.setRequest(req);
            samp.setStatus(s);
            return samp;
        }).toList();
    }

    // -------------------------------------------------------
    // Divide-by-zero defense: calculateBatchProgressPercent
    // -------------------------------------------------------

    @Test
    @DisplayName("calculateBatchProgressPercent() - totalSeconds <= 0 should return 100")
    void calculateBatchProgressPercent_divideByZero_returns100() throws Exception {
        WIPbatch batch = new WIPbatch();
        batch.setStatus("RUNNING");
        LocalDateTime now = LocalDateTime.now();
        batch.setStartTime(now);
        // set estimatedEndTime equal to startTime -> totalSeconds == 0
        batch.setEstimatedEndTime(now);

        Method m = ManagerDashboardService.class.getDeclaredMethod("calculateBatchProgressPercent", WIPbatch.class);
        m.setAccessible(true);
        Integer percent = (Integer) m.invoke(managerDashboardService, batch);

        assertEquals(100, percent.intValue());
    }

    // -------------------------------------------------------
    // Exception tests for finishRunningBatch and failRunningBatch
    // -------------------------------------------------------

    @Test
    @DisplayName("finishRunningBatch() - QUEUED batch should throw")
    void finishRunningBatch_queued_shouldThrow() throws Exception {
        WIPbatch batch = new WIPbatch();
        batch.setStatus("QUEUED");

        Method m = ManagerDashboardService.class.getDeclaredMethod("finishRunningBatch", WIPbatch.class);
        m.setAccessible(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(managerDashboardService, batch);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw (RuntimeException) ite.getTargetException();
            }
        });

        assertTrue(ex.getMessage().contains("Only RUNNING batches can be finished"));
    }

    @Test
    @DisplayName("failRunningBatch() - RUNNING batch should throw")
    void failRunningBatch_running_shouldThrow() throws Exception {
        WIPbatch batch = new WIPbatch();
        batch.setStatus("RUNNING");

        Method m = ManagerDashboardService.class.getDeclaredMethod("failRunningBatch", WIPbatch.class);
        m.setAccessible(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(managerDashboardService, batch);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw (RuntimeException) ite.getTargetException();
            }
        });

        assertTrue(ex.getMessage().contains("Only RUNNING_CRASH batches can fail"));
    }

    // -------------------------------------------------------
    // Aggregation tests: usage rate and failure rate
    // -------------------------------------------------------

    @Test
    @DisplayName("getEquipmentUsage() - usage rate calculation is correct (30.0)")
    void getEquipmentUsage_usageRateCalculation() {
        Equipment eqA = buildEquipment(1L, "EQ-A", 100L);
        Equipment eqB = buildEquipment(2L, "EQ-B", 100L);

        // create 10 batches, first 3 belong to eqA
        List<WIPbatch> allBatches = java.util.stream.IntStream.rangeClosed(1,10)
                .mapToObj(i -> {
                    WIPbatch b = new WIPbatch();
                    b.setId((long) i);
                    b.setEquipment(i <= 3 ? eqA : eqB);
                    b.setStatus("FINISHED");
                    return b;
                }).toList();

        when(wipbatchRepository.findAll()).thenReturn(allBatches);
        when(equipmentRepository.findAll()).thenReturn(List.of(eqA, eqB));

        com.example.demo.modules.wip_builder.model.EquipmentStatusLogs log = new com.example.demo.modules.wip_builder.model.EquipmentStatusLogs();
        log.setStatus("FREE");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(anyLong()))
                .thenReturn(Optional.of(log));

        List<EquipmentUsageDTO> dtos = managerDashboardService.getEquipmentUsage();

        EquipmentUsageDTO aDto = dtos.stream().filter(d -> d.getEquipmentId().equals(eqA.getId())).findFirst().orElseThrow();
        assertEquals(30.0, aDto.getUsageRate());
    }

    @Test
    @DisplayName("getEquipmentUsage() - failure rate fallback when usageCount=0 returns 0.0")
    void getEquipmentUsage_failureRateFallback_zeroUsage_shouldBeZero() {
        Equipment newEq = buildEquipment(99L, "NEW-EQ", 500L);

        // no batches at all
        when(wipbatchRepository.findAll()).thenReturn(List.of());
        when(equipmentRepository.findAll()).thenReturn(List.of(newEq));

        com.example.demo.modules.wip_builder.model.EquipmentStatusLogs log = new com.example.demo.modules.wip_builder.model.EquipmentStatusLogs();
        log.setStatus("FREE");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(newEq.getId()))
                .thenReturn(Optional.of(log));

        List<EquipmentUsageDTO> dtos = managerDashboardService.getEquipmentUsage();
        EquipmentUsageDTO dto = dtos.get(0);
        assertEquals(0.0, dto.getFailureRate());
    }
}
