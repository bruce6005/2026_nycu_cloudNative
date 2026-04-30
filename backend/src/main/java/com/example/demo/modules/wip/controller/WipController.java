package com.example.demo.modules.wip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.wip.dto.WIPBatchDTO;
import com.example.demo.modules.wip.service.WipService;

@RestController
@RequestMapping("/api/wip")
public class WipController {

    private final WipService wipService;

    public WipController(WipService wipService) {
        this.wipService = wipService;
    }

    @GetMapping
    public List<WIPBatchDTO> getWIPBatches() {
        return wipService.getWIPBatches();
    }

    @PatchMapping("/{id}/start")
    public WIPBatchDTO startBatch(@PathVariable Long id) {
        return wipService.startBatch(id);
    }

    @PatchMapping("/{id}/finish")
    public WIPBatchDTO finishBatch(@PathVariable Long id) {
        return wipService.finishBatch(id);
    }
}
