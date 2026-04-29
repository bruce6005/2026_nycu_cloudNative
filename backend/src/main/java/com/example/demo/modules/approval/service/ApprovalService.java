package com.example.demo.modules.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.approval.dto.ApprovalActionRequest;
import com.example.demo.modules.approval.dto.ApprovalResponse;
import com.example.demo.modules.tempdb.model.Request;
import com.example.demo.modules.tempdb.repository.RequestRepository;

@Service
public class ApprovalService {

    private final RequestRepository requestsRepository;
    public ApprovalService(RequestRepository requestsRepository) {
        this.requestsRepository = requestsRepository;
    }

    // 查 pending（只查這個 approver 的）
    public List<ApprovalResponse> getPendingRequest(Long approverId) {
        return requestsRepository
                .findByApprover_IdAndStatus(approverId, "PENDING")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // approve / reject 主邏輯
    public void handle(Long id, ApprovalActionRequest request) {

        Request req = requestsRepository.findById(id)
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
                break;

            default:
                throw new RuntimeException("Invalid action");
        }

        req.setEndTime(LocalDateTime.now());

        requestsRepository.save(req);

    }

    // entity → DTO
    private ApprovalResponse toResponse(Request req) {
        ApprovalResponse res = new ApprovalResponse();
        res.setId(req.getId());
        res.setFactoryUserId(req.getFactoryUser() != null ? req.getFactoryUser().getId() : null);
        res.setApproverId(req.getApprover() != null ? req.getApprover().getId() : null);
        res.setTitle(req.getTitle());
        
        // 嘗試將優先度 String 轉回 Integer
        try {
            res.setPriority(Integer.parseInt(req.getPriority()));
        } catch (Exception e) {
            res.setPriority(5);
        }

        res.setStatus(req.getStatus());
        res.setDescription(req.getDescription());
        res.setCreateTime(req.getCreateTime());
        return res;
    }
}