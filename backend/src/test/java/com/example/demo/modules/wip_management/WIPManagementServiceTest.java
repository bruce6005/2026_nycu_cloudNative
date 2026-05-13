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
 * 測試範疇：
 * - getWIPBatches() 正常查詢 / 空列表
 * - startBatch() 正常啟動 / 狀態不符拋例外 / 找不到拋例外
 * - finishBatch() 正常完成 / 狀態不符拋例外 / 找不到拋例外
 * - checkAndUpdateRequestStatus() 各種樣本狀態組合
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

    @Test
    @DisplayName("finishBatch() - RUNNING 狀態應成功完成並廣播通知")
    void finishBatch_running_shouldFinishAndBroadcast() {
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch batch = buildBatch(20L, "RUNNING", eq, recipe);
        batch.setStartTime(LocalDateTime.now().minusMinutes(1));
        batch.setEstimatedEndTime(LocalDateTime.now().plusMinutes(5));

        when(wipbatchRepository.findById(20L)).thenReturn(Optional.of(batch));
        when(wipbatchRepository.save(any(WIPbatch.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sampleRepository.findByBatch_Id(20L)).thenReturn(Collections.emptyList());
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(1L))
                .thenReturn(Optional.empty());

        WIPBatchDTO result = wipManagementService.finishBatch(20L);

        assertNotNull(result);
        assertEquals("FINISHED", result.getStatus());
        verify(notificationService, times(1)).broadcast(eq("REQUEST_UPDATED"), anyString());
    }

    @Test
    @DisplayName("finishBatch() - RUNNING_CRASH 狀態手動呼叫應拋 RuntimeException")
    void finishBatch_runningCrash_shouldThrow() {
        Equipment eq = buildEquipment(1L);
        Recipe recipe = buildRecipe(1L);
        WIPbatch batch = buildBatch(20L, "RUNNING_CRASH", eq, recipe);

        when(wipbatchRepository.findById(20L)).thenReturn(Optional.of(batch));

        assertThrows(RuntimeException.class, () -> wipManagementService.finishBatch(20L));
        verify(notificationService, never()).broadcast(anyString(), anyString());
    }

    @Test
    @DisplayName("finishBatch() - 找不到 batch 時應拋 RuntimeException")
    void finishBatch_notFound_shouldThrow() {
        when(wipbatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> wipManagementService.finishBatch(999L));
    }

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
