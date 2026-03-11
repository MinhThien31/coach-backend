package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.CoachDashboardResponse;
import com.minhthien.web.coach.dto.response.DashboardStatsResponse;

public interface DashboardService {

    DashboardStatsResponse getStats();

    CoachDashboardResponse getCoachDashboard();
}