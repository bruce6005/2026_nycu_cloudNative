package com.example.demo.modules.manager_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentUsageDTO {
    private Long equipmentId;
    private String equipmentName;
    private String equipmentType;

    private long runningMinutes;
    private long totalMinutes;
    private double usageRate;
    private String currentStatus;

    private long usageCount;
    private long totalUsageCount;
    private long averageRunSeconds;

    private long successCount;
    private long failedCount;
    private double failureRate;

    private Long activeBatchId;
    private String activeBatchStatus;
    private double activeProgressPercent;
    private long remainingSeconds;
}