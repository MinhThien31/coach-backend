package com.minhthien.web.coach.service;

import com.minhthien.web.coach.entity.TraineeSubmission;
import org.springframework.web.multipart.MultipartFile;

public interface TraineeSubmissionService {
    TraineeSubmission submitVideo(
            Long bookingId,
            Long traineeId,
            String note,
            MultipartFile file
    );
}
