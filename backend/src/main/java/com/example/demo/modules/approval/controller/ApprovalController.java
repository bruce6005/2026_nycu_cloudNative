package com.example.demo.modules.approval.controller;

import com.example.demo.modules.approval.dto.*;
import com.example.demo.modules.approval.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    // 查待審核
    @GetMapping("/pending")
    public List<ApprovalResponse> getPending(@RequestParam Long approverId) {
        return approvalService.getPendingRequest(approverId);
    }

    // approve / reject 共用
    @PostMapping("/{id}")
    public void handle(@PathVariable Long id,
                       @RequestBody ApprovalActionRequest request) {
        approvalService.handle(id, request);
    }

    @GetMapping("/test")
    public String test() {
        return "ok";
    }
}