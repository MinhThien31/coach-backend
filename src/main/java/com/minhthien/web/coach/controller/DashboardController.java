package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.CoachDashboardResponse;
import com.minhthien.web.coach.dto.response.DashboardStatsResponse;
import com.minhthien.web.coach.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/Traine")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/traines/stats")
    public DashboardStatsResponse stats() {
        return dashboardService.getStats();
    }

    @GetMapping("/coach/stats")
    public CoachDashboardResponse getDashboard() {
        return dashboardService.getCoachDashboard();
    }
}