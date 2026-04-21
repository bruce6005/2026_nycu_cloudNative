package com.example.demo.modules.approval.dto;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalResponse {
    private Long id;
    private Long factoryUserId;
    private Long approverId;
    private String status;
    private String description;
    private LocalDateTime createTime;
}
