package com.minhthien.web.coach.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.dto.response.SubmissionItemResponse;
import com.minhthien.web.coach.dto.response.VideoDetailResponse;
import com.minhthien.web.coach.entity.*;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.CoachVideoRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.CoachVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoachVideoServiceImpl implements CoachVideoService {
    private final Cloudinary cloudinary;
    private final CoachVideoRepository coachVideoRepository;
    private final UserRepository userRepository;
    private final CoachRepository coachRepository;
    private final TraineeSubmissionRepository traineeSubmissionRepository;


    @Override
    public CoachVideo uploadVideo(Long coachId, String title, String format, String resolution, List<String> tags,VideoType videoType, MultipartFile file) {
        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String videoUrl = uploadResult.get("secure_url").toString();

            double sizeGB = file.getSize() / (1024.0 * 1024 * 1024);

            User coach = userRepository.findById(coachId)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));

            CoachProfile coachProfile = coachRepository
                    .findByUserId(coachId)
                    .orElseThrow(() -> new RuntimeException("Coach profile not found"));

            Category category = coachProfile.getCategory();

            CoachVideo video = CoachVideo.builder()
                    .title(title)
                    .videoUrl(videoUrl)
                    .format(format)
                    .resolution(resolution)
                    .size(sizeGB)
                    .uploadDate(LocalDate.now())
                    .tags(String.join(",", tags))
                    .category(category)
                    .coach(coach)
                    .videoType(videoType)
                    .build();

            return coachVideoRepository.save(video);

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed");
        }
    }

    @Override
    public List<CoachVideo> getVideosByCoach(Long coachId) {
        return coachVideoRepository.findByCoachId(coachId);
    }

    @Override
    public VideoDetailResponse getVideoDetail(Long videoId) {

        CoachVideo video = coachVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        List<TraineeSubmission> submissions =
                traineeSubmissionRepository.findByCoachVideoId(videoId);

        List<SubmissionItemResponse> submissionList =
                submissions.stream()
                        .map(s -> SubmissionItemResponse.builder()
                                .submissionId(s.getId())
                                .traineeName(s.getTrainee().getFullName())
                                .avatar(s.getTrainee().getAvatarUrl())
                                .submittedAt(s.getSubmittedAt())
                                .score(s.getTotalScore())
                                .status(s.getStatus() != null ? s.getStatus().name() : "PENDING")
                                .build())
                        .toList();

        return VideoDetailResponse.builder()
                .videoId(video.getId())
                .title(video.getTitle())
                .category(video.getCategory().getName())
                .format(video.getFormat())
                .size(video.getSize())
                .resolution(video.getResolution())
                .uploadDate(video.getUploadDate())
                .tags(
                        video.getTags() != null
                                ? List.of(video.getTags().split(","))
                                : List.of()
                )
                .submissions(submissionList)
                .build();
    }

    @Override
    public List<CoachVideo> searchVideos(String keyword, VideoType type, Long coachId) {
        return coachVideoRepository.searchVideos(
                keyword,
                type,
                coachId
        );
    }

    @Override
    public CoachVideo increaseView(Long videoId) {
        CoachVideo video = coachVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Long currentViews = video.getViewCount() == null ? 0 : video.getViewCount();

        video.setViewCount(currentViews + 1);

        return coachVideoRepository.save(video);
    }

}
