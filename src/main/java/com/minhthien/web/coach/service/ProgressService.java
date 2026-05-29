package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.ProgressApiRequests;
import com.minhthien.web.coach.dto.response.ProgressApiResponses;

import java.util.List;

public interface ProgressService {

    ProgressApiResponses.ProgressOverviewResponse getOverview(Long traineeUserId);

    List<ProgressApiResponses.BodyMetricResponse> getBodyMetrics(Long traineeUserId);

    ProgressApiResponses.BodyMetricResponse createBodyMetric(Long traineeUserId, ProgressApiRequests.BodyMetricRequest request);

    List<ProgressApiResponses.ExerciseProgressResponse> getExerciseProgress(Long traineeUserId);

    ProgressApiResponses.ExerciseProgressResponse createExerciseProgress(Long traineeUserId, ProgressApiRequests.ExerciseProgressRequest request);

    List<ProgressApiResponses.AchievementResponse> getAchievements(Long traineeUserId);

    ProgressApiResponses.StreakResponse getStreak(Long traineeUserId);

    List<ProgressApiResponses.HeatmapPointResponse> getHeatmap(Long traineeUserId);
}
