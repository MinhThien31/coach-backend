package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.ProgressApiRequests;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.ProgressApiResponses;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.ProgressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees/progress")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/overview")
    public ApiResponse<ProgressApiResponses.ProgressOverviewResponse> getOverview(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getOverview(currentUser.getId()));
    }

    @GetMapping("/body-metrics")
    public ApiResponse<List<ProgressApiResponses.BodyMetricResponse>> getBodyMetrics(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getBodyMetrics(currentUser.getId()));
    }

    @PostMapping("/body-metrics")
    public ApiResponse<ProgressApiResponses.BodyMetricResponse> createBodyMetric(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ProgressApiRequests.BodyMetricRequest request
    ) {
        return ApiResponse.success(progressService.createBodyMetric(currentUser.getId(), request));
    }

    @GetMapping("/exercises")
    public ApiResponse<List<ProgressApiResponses.ExerciseProgressResponse>> getExerciseProgress(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getExerciseProgress(currentUser.getId()));
    }

    @PostMapping("/exercises")
    public ApiResponse<ProgressApiResponses.ExerciseProgressResponse> createExerciseProgress(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ProgressApiRequests.ExerciseProgressRequest request
    ) {
        return ApiResponse.success(progressService.createExerciseProgress(currentUser.getId(), request));
    }

    @GetMapping("/achievements")
    public ApiResponse<List<ProgressApiResponses.AchievementResponse>> getAchievements(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getAchievements(currentUser.getId()));
    }

    @GetMapping("/streak")
    public ApiResponse<ProgressApiResponses.StreakResponse> getStreak(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getStreak(currentUser.getId()));
    }

    @GetMapping("/heatmap")
    public ApiResponse<List<ProgressApiResponses.HeatmapPointResponse>> getHeatmap(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(progressService.getHeatmap(currentUser.getId()));
    }
}
