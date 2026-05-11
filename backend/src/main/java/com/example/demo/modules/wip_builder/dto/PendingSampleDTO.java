package com.example.demo.modules.wip_builder.dto;

import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PendingSampleDTO {
    private Long sampleId;
    private String barcode;
    private String sampleStatus;

    private Long requestId;
    private String requestTitle;
    private String requestDescription;
    private String priority;

    private Long recipeId;
    private String recipeName;
}
