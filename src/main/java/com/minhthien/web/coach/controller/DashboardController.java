package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.CoachDashboardResponse;
import com.minhthien.web.coach.dto.response.DashboardStatsResponse;
import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;
import com.minhthien.web.coach.service.DashboardService;
import com.minhthien.web.coach.service.VideoDashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/Traine")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final VideoDashboardService videoDashboardService;

    @GetMapping("/traines/stats")
    public DashboardStatsResponse stats() {
        return dashboardService.getStats();
    }

    @GetMapping("/coach/stats")
    public CoachDashboardResponse getDashboard() {
        return dashboardService.getCoachDashboard();
    }

    @GetMapping("/video/coach/dashboard")
    public VideoCoachDashboardResponse getDashboard(
            @RequestParam Long coachId
    ) {
        return videoDashboardService.getCoachDashboard(coachId);
    }
}