package com.example.demo.modules.wip_builder.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.wip_builder.dto.CreateWIPBatchRequest;
import com.example.demo.modules.wip_builder.dto.EquipmentWithRecipesDTO;
import com.example.demo.modules.wip_builder.dto.PendingSamplesGroupedByRequestDTO;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import com.example.demo.modules.wip_builder.service.WIPBuilderService;

@RestController
@RequestMapping("/api/wip_builder")
public class WIPBuilderController {

    private final WIPBuilderService wipBuilderService;

    public WIPBuilderController(WIPBuilderService wipBuilderService) {
        this.wipBuilderService = wipBuilderService;
    }


    @GetMapping("/pending")
    public List<PendingSamplesGroupedByRequestDTO> getPendingSamples() {
        return wipBuilderService.getPendingSamplesGroupedByRequest();
    }

    @GetMapping("/equipments")
    public List<EquipmentWithRecipesDTO> getEquipments() {
        return wipBuilderService.getEquipmentsWithRecipes();
    }

    @PostMapping
    public WIPBatchDTO createWIPBatch(@RequestBody CreateWIPBatchRequest request) {
        return wipBuilderService.createWIPBatch(request);
    }

}
