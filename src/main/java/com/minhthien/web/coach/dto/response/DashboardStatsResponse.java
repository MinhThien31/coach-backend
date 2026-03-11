package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {

    private long sessionsThisMonth;

    private long completedSessions;

    private long upcomingSessions;

    private double monthlySpending;

}
