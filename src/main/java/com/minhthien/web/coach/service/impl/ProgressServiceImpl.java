package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.ProgressApiRequests;
import com.minhthien.web.coach.dto.response.ProgressApiResponses;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.entity.TraineeBodyMetric;
import com.minhthien.web.coach.entity.TraineeExerciseProgress;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.TraineeBodyMetricRepository;
import com.minhthien.web.coach.repository.TraineeExerciseProgressRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TraineeBodyMetricRepository bodyMetricRepository;
    private final TraineeExerciseProgressRepository exerciseProgressRepository;
    private final TraineeSubmissionRepository submissionRepository;

    @Override
    @Transactional(readOnly = true)
    public ProgressApiResponses.ProgressOverviewResponse getOverview(Long traineeUserId) {
        long totalSessions = bookingRepository.countByTraineeId(traineeUserId);
        long activeCoaches = bookingRepository.countDistinctCoachesByTraineeId(traineeUserId);
        int streakDays = calculateStreak(traineeUserId);
        List<TraineeSubmission> scoredSubmissions = submissionRepository.findByTraineeId(traineeUserId)
                .stream()
                .filter(submission -> submission.getTotalScore() != null)
                .toList();
        int averageAiScore = scoredSubmissions.isEmpty()
                ? 0
                : (int) Math.round(scoredSubmissions.stream().mapToDouble(TraineeSubmission::getTotalScore).average().orElse(0.0) * 10);

        return ProgressApiResponses.ProgressOverviewResponse.builder()
                .totalSessions(totalSessions)
                .trainingHours(totalSessions * 2)
                .averageAiScore(averageAiScore)
                .activeCoaches(activeCoaches)
                .streakDays(streakDays)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressApiResponses.BodyMetricResponse> getBodyMetrics(Long traineeUserId) {
        return bodyMetricRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId)
                .stream()
                .map(this::mapBodyMetric)
                .toList();
    }

    @Override
    @Transactional
    public ProgressApiResponses.BodyMetricResponse createBodyMetric(Long traineeUserId, ProgressApiRequests.BodyMetricRequest request) {
        User trainee = getUser(traineeUserId);
        if (request.getWeight() == null && request.getBodyFat() == null && request.getMuscleMass() == null) {
            throw new BadRequestException("At least one body metric value is required");
        }

        TraineeBodyMetric saved = bodyMetricRepository.save(TraineeBodyMetric.builder()
                .trainee(trainee)
                .measuredAt(request.getMeasuredAt() == null ? LocalDate.now() : request.getMeasuredAt())
                .weight(request.getWeight())
                .bodyFat(request.getBodyFat())
                .muscleMass(request.getMuscleMass())
                .note(request.getNote())
                .build());
        return mapBodyMetric(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressApiResponses.ExerciseProgressResponse> getExerciseProgress(Long traineeUserId) {
        return exerciseProgressRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId)
                .stream()
                .map(this::mapExerciseProgress)
                .toList();
    }

    @Override
    @Transactional
    public ProgressApiResponses.ExerciseProgressResponse createExerciseProgress(Long traineeUserId, ProgressApiRequests.ExerciseProgressRequest request) {
        User trainee = getUser(traineeUserId);
        String exerciseName = request.getExerciseName() == null ? null : request.getExerciseName().trim();
        if (exerciseName == null || exerciseName.isEmpty()) {
            throw new BadRequestException("Exercise name is required");
        }
        if (request.getValue() == null) {
            throw new BadRequestException("Exercise progress value is required");
        }

        TraineeExerciseProgress saved = exerciseProgressRepository.save(TraineeExerciseProgress.builder()
                .trainee(trainee)
                .exerciseName(exerciseName)
                .measuredAt(request.getMeasuredAt() == null ? LocalDate.now() : request.getMeasuredAt())
                .value(request.getValue())
                .unit(request.getUnit() == null || request.getUnit().isBlank() ? "kg" : request.getUnit().trim())
                .build());
        return mapExerciseProgress(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressApiResponses.AchievementResponse> getAchievements(Long traineeUserId) {
        List<ProgressApiResponses.AchievementResponse> achievements = new ArrayList<>();
        List<Booking> bookings = bookingRepository.findByTraineeId(traineeUserId);
        List<TraineeBodyMetric> bodyMetrics = bodyMetricRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId);
        List<TraineeExerciseProgress> exercises = exerciseProgressRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId);

        bookings.stream()
                .map(Booking::getStartDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .ifPresent(date -> achievements.add(achievement(1L, "Buoi tap dau tien", "Hoan thanh buoi tap dau tien tren nen tang", date)));
        bodyMetrics.stream()
                .map(TraineeBodyMetric::getMeasuredAt)
                .min(LocalDate::compareTo)
                .ifPresent(date -> achievements.add(achievement(2L, "Theo doi co the", "Da ghi nhan chi so co the dau tien", date)));
        exercises.stream()
                .map(TraineeExerciseProgress::getMeasuredAt)
                .min(LocalDate::compareTo)
                .ifPresent(date -> achievements.add(achievement(3L, "Theo doi bai tap", "Da ghi nhan tien do bai tap dau tien", date)));
        if (calculateStreak(traineeUserId) >= 7) {
            achievements.add(achievement(4L, "Chuoi 7 ngay", "Duy tri hoat dong lien tiep 7 ngay", LocalDate.now()));
        }
        return achievements;
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressApiResponses.StreakResponse getStreak(Long traineeUserId) {
        return ProgressApiResponses.StreakResponse.builder()
                .streakDays(calculateStreak(traineeUserId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressApiResponses.HeatmapPointResponse> getHeatmap(Long traineeUserId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(89);
        Map<LocalDate, Integer> values = new HashMap<>();

        bookingRepository.findByTraineeIdAndStartDateBetween(traineeUserId, start, end)
                .forEach(booking -> addActivity(values, booking.getStartDate()));
        bodyMetricRepository.findByTraineeIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(traineeUserId, start, end)
                .forEach(metric -> addActivity(values, metric.getMeasuredAt()));
        exerciseProgressRepository.findByTraineeIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(traineeUserId, start, end)
                .forEach(progress -> addActivity(values, progress.getMeasuredAt()));
        submissionRepository.findByTraineeId(traineeUserId).stream()
                .map(TraineeSubmission::getSubmittedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(date -> !date.isBefore(start) && !date.isAfter(end))
                .forEach(date -> addActivity(values, date));

        List<ProgressApiResponses.HeatmapPointResponse> heatmap = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            heatmap.add(ProgressApiResponses.HeatmapPointResponse.builder()
                    .date(date)
                    .value(values.getOrDefault(date, 0))
                    .build());
        }
        return heatmap;
    }

    private int calculateStreak(Long traineeUserId) {
        Set<LocalDate> activeDates = new HashSet<>();
        bookingRepository.findByTraineeId(traineeUserId).stream()
                .map(Booking::getStartDate)
                .filter(Objects::nonNull)
                .forEach(activeDates::add);
        bodyMetricRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId).stream()
                .map(TraineeBodyMetric::getMeasuredAt)
                .forEach(activeDates::add);
        exerciseProgressRepository.findByTraineeIdOrderByMeasuredAtAsc(traineeUserId).stream()
                .map(TraineeExerciseProgress::getMeasuredAt)
                .forEach(activeDates::add);
        submissionRepository.findByTraineeId(traineeUserId).stream()
                .map(TraineeSubmission::getSubmittedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .forEach(activeDates::add);

        if (activeDates.isEmpty()) {
            return 0;
        }

        LocalDate cursor = activeDates.contains(LocalDate.now()) ? LocalDate.now() : activeDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        int streak = 0;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private ProgressApiResponses.BodyMetricResponse mapBodyMetric(TraineeBodyMetric metric) {
        return ProgressApiResponses.BodyMetricResponse.builder()
                .id(metric.getId())
                .measuredAt(metric.getMeasuredAt())
                .weight(metric.getWeight())
                .bodyFat(metric.getBodyFat())
                .muscleMass(metric.getMuscleMass())
                .note(metric.getNote())
                .build();
    }

    private ProgressApiResponses.ExerciseProgressResponse mapExerciseProgress(TraineeExerciseProgress progress) {
        return ProgressApiResponses.ExerciseProgressResponse.builder()
                .id(progress.getId())
                .exerciseName(progress.getExerciseName())
                .measuredAt(progress.getMeasuredAt())
                .value(progress.getValue())
                .unit(progress.getUnit())
                .build();
    }

    private ProgressApiResponses.AchievementResponse achievement(Long id, String title, String description, LocalDate achievedAt) {
        return ProgressApiResponses.AchievementResponse.builder()
                .id(id)
                .title(title)
                .description(description)
                .achievedAt(achievedAt)
                .build();
    }

    private void addActivity(Map<LocalDate, Integer> values, LocalDate date) {
        if (date != null) {
            values.merge(date, 1, Integer::sum);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
