package com.example.demo.modules.manager_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EquipmentUsageDTO {
    private Long equipmentId;
    private String equipmentName;
    private String equipmentType;

    private long usageCount;
    private long totalUsageCount;
    private double usageRate;

    private long averageRunSeconds;

    private long successCount;
    private long failedCount;
    private double failureRate;

    private String currentStatus;

    private Long activeBatchId;
    private String activeBatchStatus;
    private Integer activeProgressPercent;
    private Long remainingSeconds;
}