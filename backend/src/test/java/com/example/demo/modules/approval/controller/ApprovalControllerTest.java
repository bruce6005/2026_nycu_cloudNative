package com.example.demo.modules.approval.controller;

import com.example.demo.modules.approval.dto.ApprovalActionRequest;
import com.example.demo.modules.approval.dto.ApprovalResponse;
import com.example.demo.modules.approval.service.ApprovalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalControllerTest {

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApprovalController controller;

    @Test
    void getPending_delegatesToService_andReturnsList() {
        ApprovalResponse resp = new ApprovalResponse();
        resp.setId(7L);
        resp.setTitle("Request A");
        resp.setCreateTime(LocalDateTime.now());

        when(approvalService.getPendingRequest(1L)).thenReturn(Collections.singletonList(resp));

        List<ApprovalResponse> res = controller.getPending(1L);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(7L, res.get(0).getId());
        verify(approvalService).getPendingRequest(1L);
    }

    @Test
    void handle_delegatesToService() {
        ApprovalActionRequest req = new ApprovalActionRequest();
        req.setApproverId(2L);
        req.setAction("APPROVE");

        controller.handle(42L, req);

        verify(approvalService).handle(42L, req);
    }

    @Test
    void test_endpointReturnsOk() {
        String ok = controller.test();
        assertEquals("ok", ok);
    }
}
