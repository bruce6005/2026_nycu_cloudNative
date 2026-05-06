package com.example.demo.modules.requests.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.requests.dto.RequestsDTO;
import com.example.demo.modules.requests.service.RequestsService;

@RestController
@RequestMapping("/requests")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    /**
     * POST requests
     * 建立新的委託單
     */
    @PostMapping
    public RequestsDTO createRequest(@RequestBody RequestsDTO dto) {
        return requestsService.createRequest(dto);
    }

    /**
     * GET /api/requests/{id}
     * 取得特定委託單詳情
     */
    @GetMapping("/{id}")
    public RequestsDTO getRequestById(@PathVariable Long id) {
        return requestsService.getRequestById(id);
    }

    // RequestsController.java
    @GetMapping
    public List<RequestsDTO> getRequests(
        @RequestParam(required = false) String status
    ) {
        // 前端 call /requests?status=approved 時，會進入此邏輯
        if (status != null && !status.isEmpty()) {
            return requestsService.getByStatus(status);
        }
        
        // 如果沒有帶 status 參數，回傳全部
        return requestsService.getAllRequests();
    }

    // receive requests
    @PatchMapping("/{id}/receive")
    public ResponseEntity<RequestsDTO> receiveRequest(@PathVariable Long id) {
        RequestsDTO updatedRequest = requestsService.receiveRequest(id);

        return ResponseEntity.ok(updatedRequest);
    }
}