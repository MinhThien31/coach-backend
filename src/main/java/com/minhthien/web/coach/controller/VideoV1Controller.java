package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.VideoApiRequests;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.VideoApiResponses;
import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.service.VideoLibraryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class VideoV1Controller {

    private final VideoLibraryService videoLibraryService;

    @GetMapping("/api/v1/videos")
    public ApiResponse<List<VideoApiResponses.VideoResponse>> getVideos(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VideoType type,
            @RequestParam(required = false) Long coachId
    ) {
        return ApiResponse.success(videoLibraryService.getVideos(currentUser.getId(), keyword, type, coachId));
    }

    @GetMapping("/api/v1/videos/{id}")
    public ApiResponse<VideoApiResponses.VideoResponse> getVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.getVideo(currentUser.getId(), id));
    }

    @PostMapping("/api/v1/videos/{id}/like")
    public ApiResponse<VideoApiResponses.VideoResponse> likeVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.likeVideo(currentUser.getId(), id));
    }

    @DeleteMapping("/api/v1/videos/{id}/like")
    public ApiResponse<VideoApiResponses.VideoResponse> unlikeVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.unlikeVideo(currentUser.getId(), id));
    }

    @PostMapping("/api/v1/videos/{id}/save")
    public ApiResponse<VideoApiResponses.VideoResponse> saveVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.saveVideo(currentUser.getId(), id));
    }

    @DeleteMapping("/api/v1/videos/{id}/save")
    public ApiResponse<VideoApiResponses.VideoResponse> unsaveVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.unsaveVideo(currentUser.getId(), id));
    }

    @GetMapping("/api/v1/videos/saved")
    public ApiResponse<List<VideoApiResponses.VideoResponse>> getSavedVideos(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(videoLibraryService.getSavedVideos(currentUser.getId()));
    }

    @PutMapping("/api/v1/coach/videos/{id}")
    public ApiResponse<VideoApiResponses.VideoResponse> updateCoachVideo(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestBody VideoApiRequests.UpdateCoachVideoRequest request
    ) {
        return ApiResponse.success(videoLibraryService.updateCoachVideo(currentUser.getId(), id, request));
    }

    @DeleteMapping("/api/v1/coach/videos/{id}")
    public ApiResponse<Void> deleteCoachVideo(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        videoLibraryService.deleteCoachVideo(currentUser.getId(), id);
        return ApiResponse.success("Video deleted successfully", null);
    }

    @GetMapping("/api/v1/coach/videos/dashboard")
    public ApiResponse<VideoCoachDashboardResponse> getCoachVideoDashboard(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(videoLibraryService.getCoachVideoDashboard(currentUser.getId()));
    }

    @GetMapping("/api/v1/coach/videos/{id}/analytics")
    public ApiResponse<VideoApiResponses.VideoAnalyticsResponse> getCoachVideoAnalytics(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(videoLibraryService.getCoachVideoAnalytics(currentUser.getId(), id));
    }

    @GetMapping("/api/v1/coach/submissions")
    public ApiResponse<List<VideoApiResponses.SubmissionResponse>> getCoachSubmissions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) SubmissionStatus status
    ) {
        return ApiResponse.success(videoLibraryService.getCoachSubmissions(currentUser.getId(), status));
    }

    @GetMapping("/api/v1/coach/submissions/pending")
    public ApiResponse<List<VideoApiResponses.SubmissionResponse>> getPendingCoachSubmissions(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(videoLibraryService.getCoachSubmissions(currentUser.getId(), SubmissionStatus.PENDING));
    }

    @GetMapping("/api/v1/videos/submissions/my")
    public ApiResponse<List<VideoApiResponses.SubmissionResponse>> getMyVideoSubmissions(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(videoLibraryService.getMySubmissions(currentUser.getId()));
    }

    @PostMapping(value = "/api/v1/videos/{id}/submissions", consumes = "multipart/form-data")
    public ApiResponse<VideoApiResponses.SubmissionResponse> submitVideoForReview(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            @RequestParam MultipartFile file
    ) {
        return ApiResponse.success(videoLibraryService.submitVideoForReview(currentUser.getId(), id, note, file));
    }

    @PutMapping("/api/v1/coach/submissions/{submissionId}/review")
    public ApiResponse<VideoApiResponses.SubmissionResponse> reviewSubmission(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long submissionId,
            @RequestBody VideoApiRequests.ReviewSubmissionRequest request
    ) {
        return ApiResponse.success(videoLibraryService.reviewSubmission(currentUser.getId(), submissionId, request));
    }
}
