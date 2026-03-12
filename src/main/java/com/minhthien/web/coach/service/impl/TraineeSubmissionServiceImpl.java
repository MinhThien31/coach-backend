package com.minhthien.web.coach.service.impl;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.TraineeSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraineeSubmissionServiceImpl implements TraineeSubmissionService {
    private final Cloudinary cloudinary;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TraineeSubmissionRepository submissionRepository;

    @Override
    public TraineeSubmission submitVideo(Long bookingId, Long traineeId, String note, MultipartFile file) {
        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String videoUrl = uploadResult.get("secure_url").toString();

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            User trainee = userRepository.findById(traineeId)
                    .orElseThrow(() -> new RuntimeException("Trainee not found"));

            CoachProfile coachProfile = booking.getCoach();
            User coach = coachProfile.getUser();

            TraineeSubmission submission = TraineeSubmission.builder()
                    .videoUrl(videoUrl)
                    .note(note)
                    .submittedAt(LocalDateTime.now())
                    .trainee(trainee)
                    .coach(coach)
                    .booking(booking)
                    .build();

            return submissionRepository.save(submission);

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed");
        }
    }
}
