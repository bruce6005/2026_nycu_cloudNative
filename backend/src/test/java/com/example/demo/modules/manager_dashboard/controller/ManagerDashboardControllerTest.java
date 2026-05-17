package com.example.demo.modules.manager_dashboard.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.modules.manager_dashboard.dto.EquipmentUsageDTO;
import com.example.demo.modules.manager_dashboard.dto.RequestStatsDTO;
import com.example.demo.modules.manager_dashboard.dto.TestRecordLogDTO;
import com.example.demo.modules.manager_dashboard.service.ManagerDashboardService;

@ExtendWith(MockitoExtension.class)
class ManagerDashboardControllerTest {

    @Mock
    private ManagerDashboardService managerDashboardService;

    @InjectMocks
    private ManagerDashboardController controller;

    @Test
    @DisplayName("getRequestStats() - should return stats DTO")
    void getRequestStats_shouldReturnDto() {
        RequestStatsDTO dto = new RequestStatsDTO(10, 1,2,3,4,0);
        when(managerDashboardService.getRequestStats()).thenReturn(dto);

        RequestStatsDTO res = controller.getRequestStats();

        assertNotNull(res);
        assertEquals(10, res.getTotalRequests());
        verify(managerDashboardService, times(1)).getRequestStats();
    }

    @Test
    @DisplayName("getEquipmentUsage() - should return equipment usage list")
    void getEquipmentUsage_shouldReturnList() {
        EquipmentUsageDTO dto = new EquipmentUsageDTO(
            1L,        // equipmentId
            "EQ-1",   // equipmentName
            "TYPE",   // equipmentType

            3L,        // runningMinutes
            10L,       // totalMinutes
            30.0,      // usageRate
            "FREE",    // currentStatus

            100L,      // usageCount
            3L,        // totalUsageCount
            0L,        // averageRunSeconds

            0L,        // successCount
            0L,        // failedCount
            0.0,       // failureRate

            null,      // activeBatchId
            null,      // activeBatchStatus
            0.0,       // activeProgressPercent
            0L         // remainingSeconds
        );
        when(managerDashboardService.getEquipmentUsage()).thenReturn(List.of(dto));

        List<EquipmentUsageDTO> res = controller.getEquipmentUsage();

        assertEquals(1, res.size());
        assertEquals("EQ-1", res.get(0).getEquipmentName());
        verify(managerDashboardService, times(1)).getEquipmentUsage();
    }

    @Test
    @DisplayName("getTestRecordLogs() - should return test record logs list")
    void getTestRecordLogs_shouldReturnList() {
        TestRecordLogDTO dto = new TestRecordLogDTO(1L, 500L, 1L, "EQ-1", 10L, "op", "QUEUED", "{}", LocalDateTime.now(), null);
        when(managerDashboardService.getTestRecordLogs()).thenReturn(List.of(dto));

        List<TestRecordLogDTO> res = controller.getTestRecordLogs();

        assertEquals(1, res.size());
        assertEquals(500L, res.get(0).getBatchId());
        verify(managerDashboardService, times(1)).getTestRecordLogs();
    }
}
