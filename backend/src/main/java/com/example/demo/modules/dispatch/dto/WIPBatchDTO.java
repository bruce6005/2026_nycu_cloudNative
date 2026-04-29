package com.example.demo.modules.dispatch.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WIPBatchDTO {
    private Long id;
    private Long recipeId;
    private String status;
    private LocalDateTime createTime;
}
