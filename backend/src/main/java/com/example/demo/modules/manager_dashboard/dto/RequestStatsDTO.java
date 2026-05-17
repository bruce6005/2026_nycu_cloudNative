package com.example.demo.modules.manager_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestStatsDTO {
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long dispatchedRequests;
    private long completedRequests;
    private long rejectedRequests;
}