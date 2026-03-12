package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.ReviewSubmissionRequest;
import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.service.TraineeSubmissionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainee/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TraineeSubmissionController {

    private final TraineeSubmissionService submissionService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public TraineeSubmission submitVideo(
            @RequestParam Long coachVideoId,
            @RequestParam Long bookingId,
            @RequestParam Long traineeId,
            @RequestParam(required = false) String note,
            @RequestParam MultipartFile file
    ) {

        return submissionService.submitVideo(
                coachVideoId,
                bookingId,
                traineeId,
                note,
                file
        );
    }

    @GetMapping("/coach/videos/{videoId}/submissions")
    public List<TraineeSubmission> getSubmissions(@PathVariable Long videoId) {
        return submissionService.getSubmissionsByVideo(videoId);
    }

    @PutMapping("/coach/submissions/{submissionId}/review")
    public TraineeSubmission reviewSubmission(
            @PathVariable Long submissionId,
            @RequestBody ReviewSubmissionRequest request
    ) {

        return submissionService.reviewSubmission(submissionId, request);
    }
}
