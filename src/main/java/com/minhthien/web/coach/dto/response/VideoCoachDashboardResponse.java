package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCoachDashboardResponse {
    private Long totalVideos;

    private Long total360Videos;

    private Long totalViews;

    private Long totalSubmissions;

    private Long pendingReviews;
}
