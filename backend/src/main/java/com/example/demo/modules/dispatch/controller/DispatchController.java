package com.example.demo.modules.dispatch.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.dispatch.dto.CreateWIPBatchRequest;
import com.example.demo.modules.dispatch.dto.EquipmentWithRecipesDTO;
import com.example.demo.modules.dispatch.dto.PendingSamplesGroupedByRequestDTO;
import com.example.demo.modules.dispatch.dto.WIPBatchDTO;
import com.example.demo.modules.dispatch.service.DispatchService;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping
    public List<WIPBatchDTO> getWIPBatches() {
        return dispatchService.getWIPBatches();
    }

    @GetMapping("/pending")
    public List<PendingSamplesGroupedByRequestDTO> getPendingSamples() {
        return dispatchService.getPendingSamplesGroupedByRequest();
    }

    @GetMapping("/equipments")
    public List<EquipmentWithRecipesDTO> getEquipments() {
        return dispatchService.getEquipmentsWithRecipes();
    }

    @PostMapping
    public WIPBatchDTO createWIPBatch(@RequestBody CreateWIPBatchRequest request) {
        return dispatchService.createWIPBatch(request);
    }
}
