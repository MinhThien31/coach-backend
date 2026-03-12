package com.minhthien.web.coach.service;


import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;

public interface VideoDashboardService {
    VideoCoachDashboardResponse getCoachDashboard(Long coachId);
}
