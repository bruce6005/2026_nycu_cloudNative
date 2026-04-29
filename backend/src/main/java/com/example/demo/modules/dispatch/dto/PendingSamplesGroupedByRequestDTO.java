package com.example.demo.modules.dispatch.dto;

import java.util.List;

import lombok.Data;

@Data
public class PendingSamplesGroupedByRequestDTO {
    private Long requestId;
    private String requestTitle;
    private String requestDescription;
    private String priority;
    private int pendingSampleCount;
    private List<Long> unassignedSampleIds;
}
