package com.example.demo.modules.approval.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalActionRequest {
    private Long approverId;
    
    @NotBlank
    private String action; // APPROVE / REJECT
    private String reason; // optional
}