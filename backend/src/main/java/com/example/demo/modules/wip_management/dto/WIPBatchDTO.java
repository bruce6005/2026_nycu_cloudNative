package com.example.demo.modules.wip_management.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class WIPBatchDTO {
    private Long id;
    private Long recipeId;
    private String recipeName;
    private Long equipmentId;
    private String equipmentName;
    private List<String> sampleBarcodes;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
