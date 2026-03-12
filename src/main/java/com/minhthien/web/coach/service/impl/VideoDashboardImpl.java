package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.repository.CoachVideoRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.service.VideoDashboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoDashboardImpl implements VideoDashboard {

    private final CoachVideoRepository coachVideoRepository;
    private final TraineeSubmissionRepository traineeSubmissionRepository;

    @Override
    public VideoCoachDashboardResponse getCoachDashboard(Long coachId) {
        long totalVideos =
                coachVideoRepository.countByCoachId(coachId);

        long total360Videos =
                coachVideoRepository.countByCoachIdAndVideoType(
                        coachId,
                        VideoType.VR360
                );

        long totalViews =
                coachVideoRepository.getTotalViewsByCoach(coachId);

        long totalSubmissions =
                traineeSubmissionRepository.countByCoachId(coachId);

        long pendingReviews =
                traineeSubmissionRepository.countByCoachIdAndStatus(
                        coachId,
                        SubmissionStatus.PENDING
                );

        return VideoCoachDashboardResponse.builder()
                .totalVideos(totalVideos)
                .total360Videos(total360Videos)
                .totalViews(totalViews)
                .totalSubmissions(totalSubmissions)
                .pendingReviews(pendingReviews)
                .build();
    }
}
