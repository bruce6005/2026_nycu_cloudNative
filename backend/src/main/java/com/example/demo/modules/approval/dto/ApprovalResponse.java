package com.example.demo.modules.approval.dto;
import java.time.LocalDateTime;

import com.example.demo.modules.requests.model.RequestsStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalResponse {
    private Long id;
    private Long factoryUserId;
    private Long approverId;

    private String title;
    private Integer priority;
    
    private RequestsStatus status;
    private String description;
    private LocalDateTime createTime;
}
