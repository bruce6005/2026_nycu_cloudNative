package com.example.demo.modules.requests.controller;

import com.example.demo.modules.requests.dto.RequestsDTO;
import com.example.demo.modules.requests.service.RequestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    /**
     * POST /api/requests
     * 建立新的委託單
     */
    @PostMapping
    public RequestsDTO createRequest(@RequestBody RequestsDTO dto) {
        return requestsService.createRequest(dto);
    }

    /**
     * GET /api/requests
     * 取得所有委託單列表
     */
    @GetMapping
    public List<RequestsDTO> getAllRequests() {
        return requestsService.getAllRequests();
    }

    /**
     * GET /api/requests/{id}
     * 取得特定委託單詳情
     */
    @GetMapping("/{id}")
    public RequestsDTO getRequestById(@PathVariable Long id) {
        return requestsService.getRequestById(id);
    }

    // receive requests
    @PatchMapping("/{id}/receive")
    public ResponseEntity<RequestsDTO> receiveRequest(@PathVariable Long id) {
        RequestsDTO updatedRequest = requestsService.receiveRequest(id);

        return ResponseEntity.ok(updatedRequest);
    }
}