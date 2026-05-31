package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.VideoApiRequests;
import com.minhthien.web.coach.dto.response.VideoApiResponses;
import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoLibraryService {
    List<VideoApiResponses.VideoResponse> getVideos(Long currentUserId, String keyword, VideoType type, Long coachId);
    VideoApiResponses.VideoResponse getVideo(Long currentUserId, Long id);
    VideoApiResponses.VideoResponse likeVideo(Long currentUserId, Long id);
    VideoApiResponses.VideoResponse unlikeVideo(Long currentUserId, Long id);
    VideoApiResponses.VideoResponse saveVideo(Long currentUserId, Long id);
    VideoApiResponses.VideoResponse unsaveVideo(Long currentUserId, Long id);
    List<VideoApiResponses.VideoResponse> getSavedVideos(Long currentUserId);

    VideoApiResponses.VideoResponse updateCoachVideo(Long currentUserId, Long id, VideoApiRequests.UpdateCoachVideoRequest request);
    void deleteCoachVideo(Long currentUserId, Long id);
    VideoCoachDashboardResponse getCoachVideoDashboard(Long currentUserId);
    VideoApiResponses.VideoAnalyticsResponse getCoachVideoAnalytics(Long currentUserId, Long id);
    List<VideoApiResponses.SubmissionResponse> getCoachSubmissions(Long currentUserId, SubmissionStatus status);
    List<VideoApiResponses.SubmissionResponse> getMySubmissions(Long currentUserId);
    VideoApiResponses.SubmissionResponse submitVideoForReview(Long currentUserId, Long videoId, String note, MultipartFile file);
    VideoApiResponses.SubmissionResponse reviewSubmission(Long currentUserId, Long submissionId, VideoApiRequests.ReviewSubmissionRequest request);
}
