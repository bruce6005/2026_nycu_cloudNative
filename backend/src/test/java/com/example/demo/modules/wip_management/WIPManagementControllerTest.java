package com.example.demo.modules.wip_management;

import com.example.demo.modules.wip_management.controller.WIPManagementController;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import com.example.demo.modules.wip_management.service.WIPManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WIPManagementController}.
 */
@ExtendWith(MockitoExtension.class)
class WIPManagementControllerTest {

    @Mock
    private WIPManagementService wipManagementService;

    @InjectMocks
    private WIPManagementController wipManagementController;

    @Test
    @DisplayName("getWIPBatches() 應回傳 Service 提供的列表")
    void getWIPBatches_shouldReturnList() {
        WIPBatchDTO dto = new WIPBatchDTO();
        dto.setId(1L);
        dto.setStatus("QUEUED");

        when(wipManagementService.getWIPBatches()).thenReturn(List.of(dto));

        List<WIPBatchDTO> result = wipManagementController.getWIPBatches();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(wipManagementService, times(1)).getWIPBatches();
    }

    @Test
    @DisplayName("startBatch() 應委派給 Service 並回傳更新後的 DTO")
    void startBatch_shouldReturnUpdatedBatch() {
        WIPBatchDTO expectedDto = new WIPBatchDTO();
        expectedDto.setId(5L);
        expectedDto.setStatus("RUNNING");

        when(wipManagementService.startBatch(5L)).thenReturn(expectedDto);

        WIPBatchDTO result = wipManagementController.startBatch(5L);

        assertEquals(5L, result.getId());
        assertEquals("RUNNING", result.getStatus());
        verify(wipManagementService, times(1)).startBatch(5L);
    }

    @Test
    @DisplayName("getWIPBatches() 空資料時應回傳空列表")
    void getWIPBatches_empty_shouldReturnEmptyList() {
        when(wipManagementService.getWIPBatches()).thenReturn(Collections.emptyList());

        List<WIPBatchDTO> result = wipManagementController.getWIPBatches();

        assertEquals(0, result.size());
        verify(wipManagementService, times(1)).getWIPBatches();
    }
}
