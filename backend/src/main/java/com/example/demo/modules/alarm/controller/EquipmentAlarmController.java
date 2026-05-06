package com.example.demo.modules.equipment.controller;

import com.example.demo.modules.equipment.dto.EquipmentAlarmDTO;
import com.example.demo.modules.equipment.dto.ResolveAlarmRequest;
import com.example.demo.modules.equipment.service.EquipmentAlarmService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment/alarms")
public class EquipmentAlarmController {

    private final EquipmentAlarmService equipmentAlarmService;

    public EquipmentAlarmController(EquipmentAlarmService equipmentAlarmService) {
        this.equipmentAlarmService = equipmentAlarmService;
    }

    @GetMapping("/active")
    public List<EquipmentAlarmDTO> getActiveAlarms() {
        return equipmentAlarmService.getActiveAlarms();
    }

    @PostMapping("/{id}/resolve")
    public EquipmentAlarmDTO resolveAlarm(
            @PathVariable Long id,
            @RequestBody ResolveAlarmRequest request
    ) {
        return equipmentAlarmService.resolveAlarm(id, request);
    }
}