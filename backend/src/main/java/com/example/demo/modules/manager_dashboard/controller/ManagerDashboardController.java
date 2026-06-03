package com.example.demo.modules.manager_dashboard.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.manager_dashboard.dto.EquipmentUsageDTO;
import com.example.demo.modules.manager_dashboard.dto.RequestStatsDTO;
import com.example.demo.modules.manager_dashboard.dto.TestRecordLogDTO;
import com.example.demo.modules.manager_dashboard.service.ManagerDashboardService;

@RestController
@RequestMapping("/api/manager_dashboard")
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    public ManagerDashboardController(ManagerDashboardService managerDashboardService) {
        this.managerDashboardService = managerDashboardService;
    }

    @GetMapping("/request-stats")
    public RequestStatsDTO getRequestStats() {
        return managerDashboardService.getRequestStats();
    }

    @GetMapping("/equipment-usage")
    public List<EquipmentUsageDTO> getEquipmentUsage() {
        return managerDashboardService.getEquipmentUsage();
    }

    @GetMapping("/test-records")
    public List<TestRecordLogDTO> getTestRecordLogs() {
        return managerDashboardService.getTestRecordLogs();
    }
}