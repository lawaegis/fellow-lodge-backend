package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.dashboard.DashboardStats;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('DASHBOARD:VIEW')")
    public ApiResponse<DashboardStats> stats() {
        return ApiResponse.ok(dashboardService.getStats(SecurityUtils.currentUserId()));
    }
}
