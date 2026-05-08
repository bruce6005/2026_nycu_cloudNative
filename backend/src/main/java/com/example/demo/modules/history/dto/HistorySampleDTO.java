package com.example.demo.modules.history.dto;

import lombok.Data;

@Data
public class HistorySampleDTO {
    private Long sampleId;
    private String barcode;
    private String status;
    private Long batchId;
    private String batchStatus;
    private String equipmentName;
    private String recipeName;
}
