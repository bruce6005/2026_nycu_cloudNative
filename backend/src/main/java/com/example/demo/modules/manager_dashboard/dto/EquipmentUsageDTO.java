package com.example.demo.modules.manager_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EquipmentUsageDTO {
    private Long equipmentId;
    private String equipmentName;
    private String equipmentType;
    private long runningMinutes;
    private long totalMinutes;
    private double usageRate;
    private String currentStatus;
}