package com.example.demo.modules.equipment.service;

import com.example.demo.modules.equipment.dto.EquipmentAlarmDTO;
import com.example.demo.modules.equipment.dto.ResolveAlarmRequest;
import com.example.demo.modules.equipment.model.EquipmentAlarm;
import com.example.demo.modules.equipment.repository.EquipmentAlarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipmentAlarmService {

    private final EquipmentAlarmRepository equipmentAlarmRepository;

    public EquipmentAlarmService(EquipmentAlarmRepository equipmentAlarmRepository) {
        this.equipmentAlarmRepository = equipmentAlarmRepository;
    }

    public List<EquipmentAlarmDTO> getActiveAlarms() {
        return equipmentAlarmRepository
                .findByIsResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(EquipmentAlarmDTO::fromEntity)
                .toList();
    }

    @Transactional
    public EquipmentAlarmDTO resolveAlarm(Long id, ResolveAlarmRequest request) {
        EquipmentAlarm alarm = equipmentAlarmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alarm not found: " + id));

        if (Boolean.TRUE.equals(alarm.getIsResolved())) {
            throw new RuntimeException("Alarm already resolved: " + id);
        }

        alarm.setIsResolved(true);
        alarm.setHandlerId(request.getHandlerId());
        alarm.setResolutionNotes(request.getNotes());
        alarm.setSolvedAt(LocalDateTime.now());

        EquipmentAlarm saved = equipmentAlarmRepository.save(alarm);

        return EquipmentAlarmDTO.fromEntity(saved);
    }
}