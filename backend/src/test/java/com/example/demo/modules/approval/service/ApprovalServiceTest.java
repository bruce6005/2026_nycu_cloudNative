package com.example.demo.modules.approval.service;

import com.example.demo.modules.approval.dto.ApprovalActionRequest;
import com.example.demo.modules.approval.dto.ApprovalResponse;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.request.repository.RequestRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private com.example.demo.modules.request.repository.SampleRepository sampleRepository;
    @Mock
    private com.example.demo.modules.notification.service.NotificationService notificationService;

    @InjectMocks
    private ApprovalService approvalService;

    // helpers
    private Request buildRequest(Long id, User factoryUser, User approver, String status) {
        Request r = new Request();
        r.setId(id);
        r.setFactoryUser(factoryUser);
        r.setApprover(approver);
        r.setStatus(status);
        r.setCreateTime(LocalDateTime.now());
        r.setTitle("T");
        return r;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("u" + id);
        return u;
    }

    private Sample buildSample(Long id, Request req, Recipe recipe) {
        Sample s = new Sample();
        s.setId(id);
        s.setRequest(req);
        s.setBarcode("B" + id);
        s.setRecipe(recipe);
        s.setStatus("NEW");
        return s;
    }

    // -------------------------------------------------------
    // Sad paths for handle()
    // -------------------------------------------------------

    @Test
    @DisplayName("handle() - Request not found should throw")
    void handle_requestNotFound_shouldThrow() {
        when(requestRepository.findById(123L)).thenReturn(Optional.empty());

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(1L);
        req.setAction("APPROVE");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> approvalService.handle(123L, req));
        assertTrue(ex.getMessage().contains("Request not found"));
    }

    @Test
    @DisplayName("handle() - No permission should throw")
    void handle_noPermission_shouldThrow() {
        User approver = buildUser(10L);
        Request r = buildRequest(1L, null, approver, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(999L); // mismatch
        req.setAction("APPROVE");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> approvalService.handle(1L, req));
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    @DisplayName("handle() - Already processed should throw")
    void handle_alreadyProcessed_shouldThrow() {
        User u = buildUser(2L);
        Request r = buildRequest(1L, null, u, "APPROVED");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("APPROVE");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> approvalService.handle(1L, req));
        assertTrue(ex.getMessage().contains("Already processed"));
    }

    @Test
    @DisplayName("handle() - Invalid action should throw")
    void handle_invalidAction_shouldThrow() {
        User u = buildUser(2L);
        Request r = buildRequest(1L, null, u, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("CANCEL");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> approvalService.handle(1L, req));
        assertTrue(ex.getMessage().contains("Invalid action"));
    }

    @Test
    @DisplayName("handle() - Reject without reason should throw")
    void handle_rejectWithoutReason_shouldThrow() {
        User u = buildUser(2L);
        Request r = buildRequest(1L, null, u, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("REJECT");
        req.setReason("   ");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> approvalService.handle(1L, req));
        assertTrue(ex.getMessage().contains("Reject reason is required"));
    }

    // -------------------------------------------------------
    // Happy paths for handle()
    // -------------------------------------------------------

    @Test
    @DisplayName("handle() - Approve success should set status, endTime, save and broadcast")
    void handle_approveSuccess() {
        User u = buildUser(2L);
        Request r = buildRequest(1L, null, u, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("APPROVE");

        approvalService.handle(1L, req);

        assertEquals("APPROVED", r.getStatus());
        assertNotNull(r.getEndTime());
        verify(requestRepository, times(1)).save(r);
        verify(notificationService, times(1)).broadcast(eq("REQUEST_UPDATED"), contains("APPROVED"));
    }

    @Test
    @DisplayName("handle() - Reject success should set status, reason, endTime, save and broadcast")
    void handle_rejectSuccess() {
        User u = buildUser(2L);
        Request r = buildRequest(1L, null, u, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("REJECT");
        req.setReason("Bad quality");

        approvalService.handle(1L, req);

        assertEquals("REJECTED", r.getStatus());
        assertEquals("Bad quality", r.getRejectReason());
        assertNotNull(r.getEndTime());
        verify(requestRepository, times(1)).save(r);
        verify(notificationService, times(1)).broadcast(eq("REQUEST_UPDATED"), contains("REJECTED"));
    }

    // -------------------------------------------------------
    // getPendingRequest mapping and null-safety
    // -------------------------------------------------------

    @Test
    @DisplayName("getPendingRequest() - maps samples and recipes correctly")
    void getPendingRequest_mapsSamplesCorrectly() {
        User approver = buildUser(5L);
        User factory = buildUser(6L);
        Request r = buildRequest(1L, factory, approver, "PENDING");

        Recipe recipe1 = new Recipe(); recipe1.setId(10L); recipe1.setName("R1");
        Recipe recipe2 = new Recipe(); recipe2.setId(11L); recipe2.setName("R2");

        Sample s1 = buildSample(100L, r, recipe1);
        Sample s2 = buildSample(101L, r, recipe2);

        when(requestRepository.findByApprover_IdAndStatus(5L, "PENDING")).thenReturn(List.of(r));
        when(sampleRepository.findByRequest_Id(1L)).thenReturn(List.of(s1, s2));

        List<ApprovalResponse> res = approvalService.getPendingRequest(5L);

        assertEquals(1, res.size());
        ApprovalResponse ar = res.get(0);
        assertEquals(2, ar.getSamples().size());
        assertEquals(10L, ar.getSamples().get(0).getRecipeId());
        assertEquals("R2", ar.getSamples().get(1).getRecipeName());
    }

    @Test
    @DisplayName("getPendingRequest() - null safety with missing relations and recipes")
    void getPendingRequest_nullSafety() {
        Request r = buildRequest(2L, null, null, "PENDING");

        Sample s = buildSample(200L, r, null);

        when(requestRepository.findByApprover_IdAndStatus(99L, "PENDING")).thenReturn(List.of(r));
        when(sampleRepository.findByRequest_Id(2L)).thenReturn(List.of(s));

        List<ApprovalResponse> res = approvalService.getPendingRequest(99L);

        assertEquals(1, res.size());
        ApprovalResponse ar = res.get(0);
        assertNull(ar.getFactoryUserId());
        assertNull(ar.getApproverId());
        assertEquals(1, ar.getSamples().size());
        assertNull(ar.getSamples().get(0).getRecipeId());
        assertNull(ar.getSamples().get(0).getRecipeName());
    }

    @Test
    @DisplayName("handle() - 模擬資料庫完整性錯誤，更新 request 時應拋出 DataIntegrityViolationException")
    void handle_databaseIntegrityViolation_shouldThrowException() {
        // Arrange: 建立一個合法可被 Approver 執行的 Request，使流程能到 requestRepository.save(...)
        User approver = buildUser(2L);
        Request r = buildRequest(1L, null, approver, "PENDING");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(r));

        // Fault injection: 模擬 requestRepository.save 時發生資料庫完整性錯誤
        when(requestRepository.save(any(Request.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Simulated DB Crash"));

        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("APPROVE");

        // Act & Assert: 確認例外被向上拋出
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> approvalService.handle(1L, req));
    }

}
