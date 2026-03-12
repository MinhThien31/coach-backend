package com.minhthien.web.coach.service;

import com.minhthien.web.coach.entity.CoachVideo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CoachVideoService {
    CoachVideo uploadVideo(
            Long coachId,
            String title,
            String format,
            String resolution,
            List<String> tags,
            MultipartFile file
    );
}
