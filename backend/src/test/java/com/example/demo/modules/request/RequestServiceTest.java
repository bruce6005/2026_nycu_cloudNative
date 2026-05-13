package com.example.demo.modules.request;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.model.UserRole;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.notification.service.NotificationService;
import com.example.demo.modules.recipe.repository.RecipeRepository;
import com.example.demo.modules.request.dto.RequestDTO;
import com.example.demo.modules.request.dto.SampleDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.request.service.RequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestService}.
 *
 * 測試範疇：
 *  - createRequest() 正常流程 / 各種例外情境
 *  - getAllRequest() 列表查詢
 *  - getRequestById() 查詢單筆 / 找不到時拋例外
 *  - archiveRequest() 正常封存 / 狀態不符時拋例外
 */
@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock private RequestRepository requestRepository;
    @Mock private UserRepository userRepository;
    @Mock private SampleRepository sampleRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private RequestService requestService;

    // -------------------------------------------------------
    // Helper fixtures
    // -------------------------------------------------------

    private User buildFactoryUser(Long id, Long managerId) {
        User user = new User();
        user.setId(id);
        user.setRole(UserRole.REQUESTER);
        user.setManagerId(managerId);
        return user;
    }

    private User buildManager(Long id) {
        User mgr = new User();
        mgr.setId(id);
        mgr.setRole(UserRole.MANAGER);
        return mgr;
    }

    private Request buildSavedRequest(Long id, User factoryUser, User approver) {
        Request r = new Request();
        r.setId(id);
        r.setTitle("Test Request");
        r.setStatus("PENDING");
        r.setFactoryUser(factoryUser);
        r.setApprover(approver);
        r.setPriority("NORMAL");
        r.setCreateTime(LocalDateTime.now());
        return r;
    }

    // -------------------------------------------------------
    // createRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("createRequest() - 正常流程：應儲存 Request 並廣播通知")
    void createRequest_happyPath_shouldSaveAndBroadcast() {
        // Given
        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);

        RequestDTO dto = new RequestDTO();
        dto.setTitle("Test Request");
        dto.setFactoryUserId(1L);
        dto.setPriority("NORMAL");

        SampleDTO sampleDto = new SampleDTO();
        sampleDto.setBarcode("BARCODE-001");
        dto.setSamples(List.of(sampleDto));

        Request savedRequest = buildSavedRequest(10L, factoryUser, manager);

        when(userRepository.findById(1L)).thenReturn(Optional.of(factoryUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);
        when(sampleRepository.findByRequest_Id(10L)).thenReturn(Collections.emptyList());

        // When
        RequestDTO result = requestService.createRequest(dto);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(requestRepository, times(1)).save(any(Request.class));
        verify(notificationService, times(1)).broadcast(eq("REQUEST_UPDATED"), anyString());
    }

    @Test
    @DisplayName("createRequest() - 缺少標題時應拋出 RuntimeException")
    void createRequest_missingTitle_shouldThrow() {
        RequestDTO dto = new RequestDTO();
        dto.setFactoryUserId(1L);
        dto.setSamples(List.of(new SampleDTO()));

        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(factoryUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> requestService.createRequest(dto));
        assertEquals("Request title is required", ex.getMessage());
    }

    @Test
    @DisplayName("getRequestById() - 應正確映射所有欄位到 DTO")
    void getRequestById_detailCheck_shouldMapAllFields() {
        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);
        Request r = buildSavedRequest(5L, factoryUser, manager);
        r.setDescription("Detailed description");
        r.setPriority("URGENT");

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));
        when(sampleRepository.findByRequest_Id(5L)).thenReturn(Collections.emptyList());

        RequestDTO result = requestService.getRequestById(5L);

        assertEquals("Test Request", result.getTitle());
        assertEquals("URGENT", result.getPriority());
        assertEquals("Detailed description", result.getDescription());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("createRequest() - Factory user 沒有指定 manager 時應拋 RuntimeException")
    void createRequest_noManagerAssigned_shouldThrow() {
        User factoryUser = buildFactoryUser(1L, null); // managerId = null
        RequestDTO dto = new RequestDTO();
        dto.setFactoryUserId(1L);
        dto.setSamples(List.of(new SampleDTO()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(factoryUser));

        assertThrows(RuntimeException.class, () -> requestService.createRequest(dto));
    }

    @Test
    @DisplayName("createRequest() - 沒有傳入 samples 時應拋 RuntimeException")
    void createRequest_noSamples_shouldThrow() {
        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);
        Request savedRequest = buildSavedRequest(10L, factoryUser, manager);

        RequestDTO dto = new RequestDTO();
        dto.setFactoryUserId(1L);
        dto.setTitle("Test Title"); // 補上標題，避免觸發標題檢核失敗
        dto.setSamples(Collections.emptyList()); // 空的 sample list

        when(userRepository.findById(1L)).thenReturn(Optional.of(factoryUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> requestService.createRequest(dto));
        assertEquals("At least one sample is required", ex.getMessage());
    }

    // -------------------------------------------------------
    // getAllRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("getAllRequest() - 資料庫有資料時應回傳對應 DTO 列表")
    void getAllRequest_shouldReturnMappedDTOs() {
        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);
        Request r = buildSavedRequest(1L, factoryUser, manager);

        when(requestRepository.findAll()).thenReturn(List.of(r));
        when(sampleRepository.findByRequest_Id(1L)).thenReturn(Collections.emptyList());

        List<RequestDTO> result = requestService.getAllRequest();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getAllRequest() - 資料庫無資料時應回傳空列表")
    void getAllRequest_empty_shouldReturnEmptyList() {
        when(requestRepository.findAll()).thenReturn(Collections.emptyList());

        List<RequestDTO> result = requestService.getAllRequest();

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------
    // getRequestById()
    // -------------------------------------------------------

    @Test
    @DisplayName("getRequestById() - 找到時應回傳對應 DTO")
    void getRequestById_found_shouldReturnDTO() {
        User factoryUser = buildFactoryUser(1L, 2L);
        User manager = buildManager(2L);
        Request r = buildSavedRequest(5L, factoryUser, manager);

        when(requestRepository.findById(5L)).thenReturn(Optional.of(r));
        when(sampleRepository.findByRequest_Id(5L)).thenReturn(Collections.emptyList());

        RequestDTO result = requestService.getRequestById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
    }

    @Test
    @DisplayName("getRequestById() - 找不到時應拋 RuntimeException")
    void getRequestById_notFound_shouldThrow() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> requestService.getRequestById(999L));
    }

    // -------------------------------------------------------
    // archiveRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("archiveRequest() - DONE 狀態應成功封存")
    void archiveRequest_doneStatus_shouldArchive() {
        Request r = new Request();
        r.setId(1L);
        r.setStatus("DONE");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenReturn(r);

        assertDoesNotThrow(() -> requestService.archiveRequest(1L));

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertEquals("ARCHIVED", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("archiveRequest() - COMPLETED 狀態應成功封存")
    void archiveRequest_completedStatus_shouldArchive() {
        Request r = new Request();
        r.setId(2L);
        r.setStatus("COMPLETED");

        when(requestRepository.findById(2L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any(Request.class))).thenReturn(r);

        assertDoesNotThrow(() -> requestService.archiveRequest(2L));
    }

    @Test
    @DisplayName("archiveRequest() - PENDING 狀態應拋 RuntimeException")
    void archiveRequest_pendingStatus_shouldThrow() {
        Request r = new Request();
        r.setId(3L);
        r.setStatus("PENDING");

        when(requestRepository.findById(3L)).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> requestService.archiveRequest(3L));
    }

    @Test
    @DisplayName("archiveRequest() - 找不到 Request 時應拋 RuntimeException")
    void archiveRequest_notFound_shouldThrow() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> requestService.archiveRequest(999L));
    }
}
