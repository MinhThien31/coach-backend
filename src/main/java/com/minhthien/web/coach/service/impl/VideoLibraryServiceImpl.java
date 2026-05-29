package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.VideoApiRequests;
import com.minhthien.web.coach.dto.response.VideoApiResponses;
import com.minhthien.web.coach.dto.response.VideoCoachDashboardResponse;
import com.minhthien.web.coach.entity.Category;
import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.entity.CoachVideoLike;
import com.minhthien.web.coach.entity.CoachVideoSave;
import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.CategoryRepository;
import com.minhthien.web.coach.repository.CoachVideoLikeRepository;
import com.minhthien.web.coach.repository.CoachVideoRepository;
import com.minhthien.web.coach.repository.CoachVideoSaveRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.VideoLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VideoLibraryServiceImpl implements VideoLibraryService {

    private final CoachVideoRepository coachVideoRepository;
    private final CoachVideoLikeRepository likeRepository;
    private final CoachVideoSaveRepository saveRepository;
    private final TraineeSubmissionRepository submissionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VideoApiResponses.VideoResponse> getVideos(Long currentUserId, String keyword, VideoType type, Long coachId) {
        return coachVideoRepository.searchVideos(keyword, type, coachId).stream()
                .filter(this::isPublicVideo)
                .map(video -> mapVideo(video, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse getVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        if (!isPublicVideo(video) && !isOwner(video, currentUserId)) {
            throw new UnauthorizedException("You cannot view this video");
        }
        video.setViewCount(Objects.requireNonNullElse(video.getViewCount(), 0L) + 1);
        return mapVideo(coachVideoRepository.save(video), currentUserId);
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse likeVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        User user = getUser(currentUserId);
        likeRepository.findByVideoIdAndUserId(id, currentUserId)
                .orElseGet(() -> likeRepository.save(CoachVideoLike.builder()
                        .video(video)
                        .user(user)
                        .build()));
        syncLikes(video);
        return mapVideo(video, currentUserId);
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse unlikeVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        likeRepository.deleteByVideoIdAndUserId(id, currentUserId);
        syncLikes(video);
        return mapVideo(video, currentUserId);
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse saveVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        User user = getUser(currentUserId);
        saveRepository.findByVideoIdAndUserId(id, currentUserId)
                .orElseGet(() -> saveRepository.save(CoachVideoSave.builder()
                        .video(video)
                        .user(user)
                        .build()));
        return mapVideo(video, currentUserId);
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse unsaveVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        saveRepository.deleteByVideoIdAndUserId(id, currentUserId);
        return mapVideo(video, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoApiResponses.VideoResponse> getSavedVideos(Long currentUserId) {
        return saveRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(CoachVideoSave::getVideo)
                .filter(this::isPublicVideo)
                .map(video -> mapVideo(video, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public VideoApiResponses.VideoResponse updateCoachVideo(Long currentUserId, Long id, VideoApiRequests.UpdateCoachVideoRequest request) {
        CoachVideo video = getOwnedVideo(currentUserId, id);
        if (request.getTitle() != null) video.setTitle(request.getTitle());
        if (request.getDescription() != null) video.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null) video.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getFormat() != null) video.setFormat(request.getFormat());
        if (request.getResolution() != null) video.setResolution(request.getResolution());
        if (request.getDuration() != null) video.setDuration(request.getDuration());
        if (request.getDifficulty() != null) video.setDifficulty(request.getDifficulty());
        if (request.getVisibility() != null) video.setVisibility(request.getVisibility());
        if (request.getIsPremium() != null) video.setIsPremium(request.getIsPremium());
        if (request.getVideoType() != null) video.setVideoType(request.getVideoType());
        if (request.getTags() != null) video.setTags(request.getTags());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            video.setCategory(category);
        }
        return mapVideo(coachVideoRepository.save(video), currentUserId);
    }

    @Override
    @Transactional
    public void deleteCoachVideo(Long currentUserId, Long id) {
        CoachVideo video = getOwnedVideo(currentUserId, id);
        if (!submissionRepository.findByCoachVideoId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete video with submissions");
        }
        likeRepository.deleteByVideoId(id);
        saveRepository.deleteByVideoId(id);
        coachVideoRepository.delete(video);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoCoachDashboardResponse getCoachVideoDashboard(Long currentUserId) {
        long totalVideos = coachVideoRepository.countByCoachId(currentUserId);
        long total360Videos = coachVideoRepository.countByCoachIdAndVideoType(currentUserId, VideoType.VR360);
        long totalViews = Objects.requireNonNullElse(coachVideoRepository.getTotalViewsByCoach(currentUserId), 0L);
        long totalSubmissions = submissionRepository.countByCoachId(currentUserId);
        long pendingReviews = submissionRepository.countByCoachIdAndStatus(currentUserId, SubmissionStatus.PENDING);
        return VideoCoachDashboardResponse.builder()
                .totalVideos(totalVideos)
                .total360Videos(total360Videos)
                .totalViews(totalViews)
                .totalSubmissions(totalSubmissions)
                .pendingReviews(pendingReviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VideoApiResponses.VideoAnalyticsResponse getCoachVideoAnalytics(Long currentUserId, Long id) {
        CoachVideo video = getOwnedVideo(currentUserId, id);
        List<TraineeSubmission> submissions = submissionRepository.findByCoachVideoId(id);
        List<TraineeSubmission> scored = submissions.stream()
                .filter(s -> s.getTotalScore() != null)
                .toList();
        return VideoApiResponses.VideoAnalyticsResponse.builder()
                .videoId(video.getId())
                .views(Objects.requireNonNullElse(video.getViewCount(), 0L))
                .likes(likeRepository.countByVideoId(id))
                .saves(saveRepository.countByVideoId(id))
                .submissions((long) submissions.size())
                .pendingSubmissions(submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.PENDING).count())
                .averageScore(scored.stream().mapToDouble(TraineeSubmission::getTotalScore).average().orElse(0.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoApiResponses.SubmissionResponse> getCoachSubmissions(Long currentUserId, SubmissionStatus status) {
        return submissionRepository.findByCoachId(currentUserId)
                .stream()
                .filter(submission -> status == null || submission.getStatus() == status)
                .map(this::mapSubmission)
                .toList();
    }

    private CoachVideo getVideoEntity(Long id) {
        return coachVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id));
    }

    private CoachVideo getOwnedVideo(Long currentUserId, Long id) {
        CoachVideo video = getVideoEntity(id);
        if (!isOwner(video, currentUserId)) {
            throw new UnauthorizedException("You cannot manage this video");
        }
        return video;
    }

    private User getUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
    }

    private boolean isOwner(CoachVideo video, Long currentUserId) {
        return video.getCoach() != null && video.getCoach().getId().equals(currentUserId);
    }

    private boolean isPublicVideo(CoachVideo video) {
        return video.getVisibility() == null
                || video.getVisibility().isBlank()
                || "PUBLIC".equalsIgnoreCase(video.getVisibility());
    }

    private void syncLikes(CoachVideo video) {
        video.setLikes(likeRepository.countByVideoId(video.getId()));
        coachVideoRepository.save(video);
    }

    private VideoApiResponses.VideoResponse mapVideo(CoachVideo video, Long currentUserId) {
        return VideoApiResponses.VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .category(video.getCategory() == null ? null : video.getCategory().getName())
                .categoryId(video.getCategory() == null ? null : video.getCategory().getId())
                .coachName(video.getCoach() == null ? null : video.getCoach().getFullName())
                .coachUserId(video.getCoach() == null ? null : video.getCoach().getId())
                .videoType(video.getVideoType())
                .format(video.getFormat())
                .resolution(video.getResolution())
                .size(video.getSize())
                .duration(video.getDuration())
                .difficulty(video.getDifficulty())
                .visibility(video.getVisibility())
                .viewCount(Objects.requireNonNullElse(video.getViewCount(), 0L))
                .likes(Objects.requireNonNullElse(video.getLikes(), 0L))
                .isPremium(Boolean.TRUE.equals(video.getIsPremium()))
                .liked(currentUserId != null && likeRepository.existsByVideoIdAndUserId(video.getId(), currentUserId))
                .saved(currentUserId != null && saveRepository.existsByVideoIdAndUserId(video.getId(), currentUserId))
                .tags(video.getTags() == null || video.getTags().isBlank() ? List.of() : List.of(video.getTags().split(",")))
                .uploadDate(video.getUploadDate())
                .build();
    }

    private VideoApiResponses.SubmissionResponse mapSubmission(TraineeSubmission submission) {
        return VideoApiResponses.SubmissionResponse.builder()
                .id(submission.getId())
                .videoId(submission.getCoachVideo() == null ? null : submission.getCoachVideo().getId())
                .videoTitle(submission.getCoachVideo() == null ? null : submission.getCoachVideo().getTitle())
                .traineeId(submission.getTrainee() == null ? null : submission.getTrainee().getId())
                .traineeName(submission.getTrainee() == null ? null : submission.getTrainee().getFullName())
                .videoUrl(submission.getVideoUrl())
                .note(submission.getNote())
                .status(submission.getStatus())
                .totalScore(submission.getTotalScore())
                .feedback(submission.getFeedback())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
