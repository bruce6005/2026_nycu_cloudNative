package com.example.demo.modules.wip_builder.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateWIPBatchRequest {
    private Long operatorId;
    private Long equipmentId;
    private Long recipeId;
    private List<Long> sampleIds;
}
