package com.example.demo.modules.manager_dashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestRecordLogDTO {
    private Long id;

    private Long batchId;

    private Long equipmentId;
    private String equipmentName;

    private Long operatorId;
    private String operatorName;

    private String resultStatus;
    private Object resultData;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}