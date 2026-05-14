package com.example.demo.modules.wip_management;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.notification.service.NotificationService;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import com.example.demo.modules.wip_management.service.WIPManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WIPManagementService}.
 * 
 * ----------------------------------------------------
 * 測試功能清單 (Test Coverage Summary):
 * ----------------------------------------------------
 * 1. getWIPBatches (取得批次看板 - 核心驅動):
 *    - [x] 正常查詢：應回傳所有批次列表
 *    - [x] 自動校正 (Auto-Resolve Case 1)：過期的 RUNNING 應自動變更為 FINISHED
 *    - [x] 自動校正 (Auto-Resolve Case 2)：過期的 RUNNING_CRASH 應自動變更為 FAILED
 *    - [x] 自動校正 (Auto-Resolve Case 3)：未過期的批次應保持原狀
 * 
 * 2. startBatch (啟動批次):
 *    - [x] 正常流程：QUEUED 狀態成功啟動，設備變 BUSY，發送廣播
 *    - [x] 異常情境：非 QUEUED 狀態嘗試啟動拋出例外
 * 
 * 3. finishBatch (手動結案 - 已移除功能):
 *    - [x] 功能已從系統移除，完全由 Auto-Resolve 取代
 * 
 * 4. 狀態連動檢核 (checkAndUpdateRequestStatus):
 *    - [x] 型妥善處理所有 Sample 狀態與 Request 狀態同步
 * ----------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
class WIPManagementServiceTest {

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private WIPbatchRepository wipbatchRepository;
    @Mock
    private EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WIPManagementService wipManagementService;

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private Equipment buildEquipment(Long id) {
        Equipment eq = new Equipment();
        eq.setId(id);
        eq.setName("EQ-" + id);
        return eq;
    }

    private Recipe buildRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("RECIPE-" + id);
        return recipe;
    }

    private WIPbatch buildBatch(Long id, String status, Equipment equipment, Recipe recipe) {
        WIPbatch batch = new WIPbatch();
        batch.setId(id);
        batch.setStatus(status);
        batch.setEquipment(equipment);
        batch.setRecipe(recipe);
        batch.setCreateTime(LocalDateTime.now());
        return batch;
    }

    private Sample buildSample(String status, WIPbatch batch, Request request) {
        Sample sample = new Sample();
        sample.setBarcode("BAR-001");
        sample.setStatus(status);
        sample.setBatch(batch);
        sample.setRequest(request);
        return sample;
    }

    // -------------------------------------------------------
    // getWIPBatches()
    // -------------------------------------------------------

    @Test
    @DisplayName("getWIPBatches() - 無 running batch 時應回傳所有 batch 的 DTO 列表")
    void getWIPBatches_shouldReturnDTOList() {
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch batch = buildBatch(1L, "QUEUED", eq, recipe);

        // 沒有 RUNNING / RUNNING_CRASH batch → autoResolve 不處理
        when(wipbatchRepository.findByStatusIn(anyList())).thenReturn(Collections.emptyList());
        when(wipbatchRepository.findAll(any(Sort.class))).thenReturn(List.of(batch));
        when(sampleRepository.findByBatch_Id(1L)).thenReturn(Collections.emptyList());

        List<WIPBatchDTO> result = wipManagementService.getWIPBatches();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("QUEUED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("getWIPBatches() - DB 無資料時應回傳空列表")
    void getWIPBatches_empty_shouldReturnEmptyList() {
        when(wipbatchRepository.findByStatusIn(anyList())).thenReturn(Collections.emptyList());
        when(wipbatchRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        List<WIPBatchDTO> result = wipManagementService.getWIPBatches();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Auto-Resolve Case 1: 過期的 RUNNING 批次在查詢時應自動變為 FINISHED")
    void getWIPBatches_expiredRunning_shouldAutoFinish() {
        // Given
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch expiredBatch = buildBatch(1L, "RUNNING", eq, recipe);
        expiredBatch.setEstimatedEndTime(LocalDateTime.now().minusMinutes(5)); // 已過期 5 分鐘

        when(wipbatchRepository.findByStatusIn(anyList())).thenReturn(List.of(expiredBatch));
        when(wipbatchRepository.findAll(any(Sort.class))).thenReturn(List.of(expiredBatch));
        when(sampleRepository.findByBatch_Id(1L)).thenReturn(Collections.emptyList());
        when(wipbatchRepository.save(any(WIPbatch.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<WIPBatchDTO> result = wipManagementService.getWIPBatches();

        // Then
        assertEquals("FINISHED", result.get(0).getStatus());
        verify(wipbatchRepository, atLeastOnce()).save(any(WIPbatch.class));
        verify(notificationService, atLeastOnce()).broadcast(eq("REQUEST_UPDATED"), contains("Batch finished"));
    }

    @Test
    @DisplayName("Auto-Resolve Case 2: 過期的 RUNNING_CRASH 批次在查詢時應自動變為 FAILED")
    void getWIPBatches_expiredCrash_shouldAutoFail() {
        // Given
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch expiredCrashBatch = buildBatch(2L, "RUNNING_CRASH", eq, recipe);
        expiredCrashBatch.setEstimatedEndTime(LocalDateTime.now().minusMinutes(10)); // 已過期 10 分鐘

        when(wipbatchRepository.findByStatusIn(anyList())).thenReturn(List.of(expiredCrashBatch));
        when(wipbatchRepository.findAll(any(Sort.class))).thenReturn(List.of(expiredCrashBatch));
        when(sampleRepository.findByBatch_Id(2L)).thenReturn(Collections.emptyList());
        when(wipbatchRepository.save(any(WIPbatch.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<WIPBatchDTO> result = wipManagementService.getWIPBatches();

        // Then
        assertEquals("FAILED", result.get(0).getStatus());
        verify(notificationService, atLeastOnce()).broadcast(eq("REQUEST_UPDATED"), contains("Batch failed"));
    }

    @Test
    @DisplayName("Auto-Resolve Case 3: 未過期的批次應保持狀態不變")
    void getWIPBatches_ongoingBatch_shouldStayRunning() {
        // Given
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch ongoingBatch = buildBatch(3L, "RUNNING", eq, recipe);
        ongoingBatch.setEstimatedEndTime(LocalDateTime.now().plusHours(1)); // 還有一小時才完工

        when(wipbatchRepository.findByStatusIn(anyList())).thenReturn(List.of(ongoingBatch));
        when(wipbatchRepository.findAll(any(Sort.class))).thenReturn(List.of(ongoingBatch));
        when(sampleRepository.findByBatch_Id(3L)).thenReturn(Collections.emptyList());

        // When
        List<WIPBatchDTO> result = wipManagementService.getWIPBatches();

        // Then
        assertEquals("RUNNING", result.get(0).getStatus());
        verify(wipbatchRepository, never()).save(any()); // 不應呼叫 save
    }

    // -------------------------------------------------------
    // startBatch()
    // -------------------------------------------------------

    @Test
    @DisplayName("startBatch() - QUEUED 狀態應成功啟動並廣播通知")
    void startBatch_queued_shouldStartAndBroadcast() {
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch batch = buildBatch(10L, "QUEUED", eq, recipe);

        when(wipbatchRepository.findById(10L)).thenReturn(Optional.of(batch));
        when(wipbatchRepository.save(any(WIPbatch.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sampleRepository.findByBatch_Id(10L)).thenReturn(Collections.emptyList());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(1L))
                .thenReturn(Optional.empty());

        WIPBatchDTO result = wipManagementService.startBatch(10L);

        assertNotNull(result);
        // status 會是 RUNNING 或 RUNNING_CRASH（有 25% crash 機率）
        assertTrue("RUNNING".equals(result.getStatus()) || "RUNNING_CRASH".equals(result.getStatus()));
        verify(notificationService, times(1)).broadcast(eq("REQUEST_UPDATED"), anyString());
    }

    @Test
    @DisplayName("startBatch() - 非 QUEUED 狀態應拋 RuntimeException")
    void startBatch_notQueued_shouldThrow() {
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch batch = buildBatch(10L, "RUNNING", eq, recipe);

        when(wipbatchRepository.findById(10L)).thenReturn(Optional.of(batch));

        assertThrows(RuntimeException.class, () -> wipManagementService.startBatch(10L));
        verify(notificationService, never()).broadcast(anyString(), anyString());
    }

    @Test
    @DisplayName("startBatch() - 找不到 batch 時應拋 RuntimeException")
    void startBatch_notFound_shouldThrow() {
        when(wipbatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> wipManagementService.startBatch(999L));
    }

    // -------------------------------------------------------
    // finishBatch()
    // -------------------------------------------------------


    // -------------------------------------------------------
    // checkAndUpdateRequestStatus()
    // -------------------------------------------------------

    @Test
    @DisplayName("checkAndUpdateRequestStatus() - 所有 sample COMPLETED 時 request 應變為 DONE")
    void checkAndUpdateRequestStatus_allCompleted_shouldSetDone() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus("PROCESSING");

        Sample s1 = new Sample();
        s1.setStatus("COMPLETED");
        Sample s2 = new Sample();
        s2.setStatus("COMPLETED");

        when(sampleRepository.findByRequest_Id(1L)).thenReturn(List.of(s1, s2));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        wipManagementService.checkAndUpdateRequestStatus(request);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertEquals("DONE", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("checkAndUpdateRequestStatus() - 有 sample FAILED 時 request 應變為 FAILED")
    void checkAndUpdateRequestStatus_anyFailed_shouldSetFailed() {
        Request request = new Request();
        request.setId(2L);
        request.setStatus("PROCESSING");

        Sample s1 = new Sample();
        s1.setStatus("COMPLETED");
        Sample s2 = new Sample();
        s2.setStatus("FAILED");

        when(sampleRepository.findByRequest_Id(2L)).thenReturn(List.of(s1, s2));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        wipManagementService.checkAndUpdateRequestStatus(request);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertEquals("FAILED", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("checkAndUpdateRequestStatus() - 有 sample RUNNING 且無 FAILED 時 request 應變為 PROCESSING")
    void checkAndUpdateRequestStatus_anyRunning_shouldSetProcessing() {
        Request request = new Request();
        request.setId(3L);
        request.setStatus("DISPATCHED");

        Sample s1 = new Sample();
        s1.setStatus("RUNNING");
        Sample s2 = new Sample();
        s2.setStatus("ASSIGNED");

        when(sampleRepository.findByRequest_Id(3L)).thenReturn(List.of(s1, s2));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        wipManagementService.checkAndUpdateRequestStatus(request);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertEquals("PROCESSING", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("checkAndUpdateRequestStatus() - 狀態未改變時不應呼叫 save")
    void checkAndUpdateRequestStatus_statusUnchanged_shouldNotSave() {
        Request request = new Request();
        request.setId(4L);
        request.setStatus("DONE");

        Sample s1 = new Sample();
        s1.setStatus("COMPLETED");

        when(sampleRepository.findByRequest_Id(4L)).thenReturn(List.of(s1));

        wipManagementService.checkAndUpdateRequestStatus(request);

        verify(requestRepository, never()).save(any());
    }
}
