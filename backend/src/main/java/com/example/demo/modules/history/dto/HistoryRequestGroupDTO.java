package com.example.demo.modules.history.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class HistoryRequestGroupDTO {
    private Long requestId;
    private String requestTitle;
    private String requestDescription;
    private String requestStatus;
    private String priority;
    private LocalDateTime createTime;
    private LocalDateTime endTime;
    private int sampleCount;
    private List<HistorySampleDTO> samples;
}
