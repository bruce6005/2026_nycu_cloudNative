package com.example.demo.modules.equipment.dto;

import com.example.demo.modules.equipment.model.EquipmentAlarm;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentAlarmDTO {

    private Long id;
    private Long equipmentId;
    private Long handlerId;
    private String errorCode;
    private LocalDateTime createdAt;
    private LocalDateTime solvedAt;
    private Boolean isResolved;
    private String resolutionNotes;

    public static EquipmentAlarmDTO fromEntity(EquipmentAlarm alarm) {
        EquipmentAlarmDTO dto = new EquipmentAlarmDTO();

        dto.setId(alarm.getId());
        dto.setEquipmentId(alarm.getEquipmentId());
        dto.setHandlerId(alarm.getHandlerId());
        dto.setErrorCode(alarm.getErrorCode());
        dto.setCreatedAt(alarm.getCreatedAt());
        dto.setSolvedAt(alarm.getSolvedAt());
        dto.setIsResolved(alarm.getIsResolved());
        dto.setResolutionNotes(alarm.getResolutionNotes());

        return dto;
    }
}