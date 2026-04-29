package com.example.demo.modules.dispatch.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateWIPBatchRequest {
    private Long equipmentId;
    private Long recipeId;
    private List<Long> sampleIds;
}
