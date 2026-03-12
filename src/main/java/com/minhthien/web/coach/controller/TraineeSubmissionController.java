package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.service.TraineeSubmissionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/trainee/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TraineeSubmissionController {

    private final TraineeSubmissionService submissionService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public TraineeSubmission submitVideo(
            @RequestParam Long bookingId,
            @RequestParam Long traineeId,
            @RequestParam(required = false) String note,
            @RequestParam MultipartFile file
    ) {

        return submissionService.submitVideo(
                bookingId,
                traineeId,
                note,
                file
        );
    }
}
