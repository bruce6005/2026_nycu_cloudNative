package com.example.demo.modules.request.controller;

import com.example.demo.modules.request.dto.RequestDTO;
import com.example.demo.modules.request.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/request")
public class RequestController {

    @Autowired
    private RequestService requestsService;

    /**
     * POST /api/request
     * 建立新的委託單
     */
    @PostMapping
    public RequestDTO createRequest(@RequestBody RequestDTO dto) {
        return requestsService.createRequest(dto);
    }

    /**
     * GET /api/request
     * 取得所有委託單列表
     */
    @GetMapping
    public List<RequestDTO> getAllRequest() {
        return requestsService.getAllRequest();
    }

    /**
     * GET /api/request/{id}
     * 取得特定委託單詳情
     */
    @GetMapping("/{id}")
    public RequestDTO getRequestById(@PathVariable Long id) {
        return requestsService.getRequestById(id);
    }
}
