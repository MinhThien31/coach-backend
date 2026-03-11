package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoachDashboardResponse {

    private long todaySessions;

    private long weekSessions;

    private double weekRevenue;

    private long pendingBookings;
}
