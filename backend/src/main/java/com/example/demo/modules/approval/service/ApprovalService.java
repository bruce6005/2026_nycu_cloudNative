package com.example.demo.modules.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.approval.dto.ApprovalActionRequest;
import com.example.demo.modules.approval.dto.ApprovalResponse;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.repository.RequestRepository;

@Service
public class ApprovalService {

    private final RequestRepository requestRepository;
    private final com.example.demo.modules.request.repository.SampleRepository sampleRepository;
    private final com.example.demo.modules.notification.service.NotificationService notificationService;

    public ApprovalService(RequestRepository requestRepository,
                         com.example.demo.modules.request.repository.SampleRepository sampleRepository,
                         com.example.demo.modules.notification.service.NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.sampleRepository = sampleRepository;
        this.notificationService = notificationService;
    }

    // 查 pending（只查這個 approver 的）
    public List<ApprovalResponse> getPendingRequest(Long approverId) {
        return requestRepository
                .findByApprover_IdAndStatus(approverId, "PENDING")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // approve / reject 主邏輯
    public void handle(Long id, ApprovalActionRequest request) {

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getApprover() == null || !req.getApprover().getId().equals(request.getApproverId())) {
            throw new RuntimeException("No permission");
        }

        if ("APPROVED".equals(req.getStatus()) || "REJECTED".equals(req.getStatus())) {
            throw new RuntimeException("Already processed");
        }

        switch (request.getAction()) {
            case "APPROVE":
                req.setStatus("APPROVED");
                break;

            case "REJECT":
                req.setStatus("REJECTED");
                req.setRejectReason(request.getReason());
                break;

            default:
                throw new RuntimeException("Invalid action");
        }

        req.setEndTime(LocalDateTime.now());

        requestRepository.save(req);

        // 發送更新信號
        notificationService.broadcast("REQUEST_UPDATED", "Request " + id + " state changed to " + req.getStatus());

    }

    // entity → DTO
    private ApprovalResponse toResponse(Request req) {
        ApprovalResponse res = new ApprovalResponse();
        res.setId(req.getId());
        res.setFactoryUserId(req.getFactoryUser() != null ? req.getFactoryUser().getId() : null);
        res.setApproverId(req.getApprover() != null ? req.getApprover().getId() : null);
        res.setTitle(req.getTitle());

        res.setPriority(req.getPriority());

        res.setStatus(req.getStatus());
        res.setDescription(req.getDescription());
        res.setCreateTime(req.getCreateTime());

        // 讀取 samples 並轉換為 DTO
        List<com.example.demo.modules.request.dto.SampleDTO> sampleDTOs = sampleRepository.findByRequest_Id(req.getId()).stream()
            .map(s -> {
                com.example.demo.modules.request.dto.SampleDTO sDto = new com.example.demo.modules.request.dto.SampleDTO();
                sDto.setBarcode(s.getBarcode());
                if (s.getRecipe() != null) {
                    sDto.setRecipeId(s.getRecipe().getId());
                    sDto.setRecipeName(s.getRecipe().getName());
                }
                return sDto;
            }).collect(java.util.stream.Collectors.toList());

        res.setSamples(sampleDTOs);

        return res;
    }
}