package com.minhthien.web.coach.service;


import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;

public interface VideoDashboard {
    VideoCoachDashboardResponse getCoachDashboard(Long coachId);
}
