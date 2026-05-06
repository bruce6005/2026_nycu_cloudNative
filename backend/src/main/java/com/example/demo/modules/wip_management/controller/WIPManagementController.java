package com.example.demo.modules.wip_management.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import com.example.demo.modules.wip_management.service.WIPManagementService;

@RestController
@RequestMapping("/api/wip_management")
public class WIPManagementController {

    private final WIPManagementService wipManagementService;

    public WIPManagementController(WIPManagementService wipManagementService) {
        this.wipManagementService = wipManagementService;
    }

    @GetMapping
    public List<WIPBatchDTO> getWIPBatches() {
        return wipManagementService.getWIPBatches();
    }

    @PatchMapping("/{id}/start")
    public WIPBatchDTO startBatch(@PathVariable Long id) {
        return wipManagementService.startBatch(id);
    }

    @PatchMapping("/{id}/finish")
    public WIPBatchDTO finishBatch(@PathVariable Long id) {
        return wipManagementService.finishBatch(id);
    }
}
