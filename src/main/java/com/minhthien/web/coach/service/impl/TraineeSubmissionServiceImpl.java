package com.minhthien.web.coach.service.impl;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.dto.request.CommentRequest;
import com.minhthien.web.coach.dto.request.ReviewSubmissionRequest;
import com.minhthien.web.coach.entity.*;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.repository.*;
import com.minhthien.web.coach.service.TraineeSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraineeSubmissionServiceImpl implements TraineeSubmissionService {
    private final Cloudinary cloudinary;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TraineeSubmissionRepository submissionRepository;
    private final CoachVideoRepository coachVideoRepository;
    private final SubmissionCommentRepository commentRepository;


    @Override
    public TraineeSubmission submitVideo(Long coachVideoId, Long bookingId, Long traineeId, String note, MultipartFile file) {
        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String videoUrl = uploadResult.get("secure_url").toString();

            CoachVideo coachVideo = coachVideoRepository.findById(coachVideoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            User trainee = userRepository.findById(traineeId)
                    .orElseThrow(() -> new RuntimeException("Trainee not found"));

            CoachProfile coachProfile = booking.getCoach();
            User coach = coachProfile.getUser();

            TraineeSubmission submission = TraineeSubmission.builder()
                    .coachVideo(coachVideo)
                    .videoUrl(videoUrl)
                    .note(note)
                    .submittedAt(LocalDateTime.now())
                    .trainee(trainee)
                    .coach(coach)
                    .booking(booking)
                    .status(SubmissionStatus.PENDING)
                    .build();

            return submissionRepository.save(submission);

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed");
        }
    }

    @Override
    public List<TraineeSubmission> getSubmissionsByVideo(Long videoId) {
        return submissionRepository.findByCoachVideoId(videoId);
    }

    @Override
    public TraineeSubmission reviewSubmission(Long submissionId, ReviewSubmissionRequest request) {

        TraineeSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setPostureScore(request.getPostureScore());
        submission.setTechniqueScore(request.getTechniqueScore());
        submission.setRhythmScore(request.getRhythmScore());
        submission.setStrengthScore(request.getStrengthScore());

        double total =
                (request.getPostureScore() +
                        request.getTechniqueScore() +
                        request.getRhythmScore() +
                        request.getStrengthScore()) / 4.0;

        submission.setTotalScore(total);
        submission.setFeedback(request.getFeedback());

        // set status

        if (total >= 6.5) {
            submission.setStatus(SubmissionStatus.PASSED);
        } else {
            submission.setStatus(SubmissionStatus.FAILED);
        }

        submissionRepository.save(submission);

        // save new comments

        if (request.getComments() != null) {
            for (CommentRequest c : request.getComments()) {

                SubmissionComment comment =
                        SubmissionComment.builder()
                                .submission(submission)
                                .timeSecond(c.getTimeSecond())
                                .comment(c.getComment())
                                .build();

                commentRepository.save(comment);
            }
        }

        return submission;
    }
}