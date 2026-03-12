package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.VideoDetailResponse;
import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.enums.VideoType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CoachVideoService {
    CoachVideo uploadVideo(
            Long coachId,
            String title,
            String format,
            String resolution,
            List<String> tags,
            VideoType videoType,
            MultipartFile file
    );
    List<CoachVideo> getVideosByCoach(Long coachId);

    VideoDetailResponse getVideoDetail(Long videoId);
    List<CoachVideo> searchVideos(
            String keyword,
            VideoType type,
            Long coachId
    );
    CoachVideo increaseView(Long videoId);
}
