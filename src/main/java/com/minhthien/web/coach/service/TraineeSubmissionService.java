package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.ReviewSubmissionRequest;
import com.minhthien.web.coach.entity.TraineeSubmission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TraineeSubmissionService {
    TraineeSubmission submitVideo(
            Long coachVideoId,
            Long bookingId,
            Long traineeId,
            String note,
            MultipartFile file
    );

    List<TraineeSubmission> getSubmissionsByVideo(Long videoId);

    TraineeSubmission reviewSubmission(
            Long submissionId,
            ReviewSubmissionRequest request
    );
}
