package com.example.demo.modules.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.approval.dto.ApprovalActionRequest;
import com.example.demo.modules.approval.dto.ApprovalResponse;
import com.example.demo.modules.requests.model.Requests;
import com.example.demo.modules.requests.model.RequestsStatus;
import com.example.demo.modules.requests.repository.RequestsRepository;

@Service
public class ApprovalService {

    private final RequestsRepository requestsRepository;
    public ApprovalService(RequestsRepository requestsRepository) {
        this.requestsRepository = requestsRepository;
    }

    // 查 pending（只查這個 approver 的）
    public List<ApprovalResponse> getPendingRequests(Long approverId) {
        return requestsRepository
                .findByApproverIdAndStatus(approverId, RequestsStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // approve / reject 主邏輯
    public void handle(Long id, ApprovalActionRequest request) {

        Requests req = requestsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!req.getApproverId().equals(request.getApproverId())) {
            throw new RuntimeException("No permission");
        }

        if (req.getStatus() == RequestsStatus.APPROVED || req.getStatus() == RequestsStatus.REJECTED) {
            throw new RuntimeException("Already processed");
        }

        switch (request.getAction()) {
            case "APPROVE":
                req.setStatus(RequestsStatus.APPROVED);
                break;

            case "REJECT":
                req.setStatus(RequestsStatus.REJECTED);
                // 如果你 DB 有欄位可以存 reason
                // req.setRejectReason(request.getReason());
                break;

            default:
                throw new RuntimeException("Invalid action");
        }

        req.setEndTime(LocalDateTime.now());

        requestsRepository.save(req);

    }

    // entity → DTO
    private ApprovalResponse toResponse(Requests req) {
        ApprovalResponse res = new ApprovalResponse();
        res.setId(req.getId());
        res.setFactoryUserId(req.getFactoryUserId());
        res.setApproverId(req.getApproverId());
        res.setTitle(req.getTitle());
        res.setPriority(req.getPriority());
        res.setStatus(req.getStatus());
        res.setDescription(req.getDescription());
        res.setCreateTime(req.getCreateTime());
        return res;
    }
}