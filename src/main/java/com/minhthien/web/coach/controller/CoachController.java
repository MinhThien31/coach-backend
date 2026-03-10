package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.*;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.service.CoachService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CoachController {

    private final CoachService coachService;

    @PostMapping("/search")
    public ApiResponse<Page<CoachResponse>> searchCoach(
            @RequestBody CoachSearchRequest request
    ) {
        return ApiResponse.<Page<CoachResponse>>builder()
                .success(true)
                .data(coachService.searchCoach(request))
                .build();
    }

    @GetMapping("/coachDetail/{id}")
    public ApiResponse<CoachDetailResponse> getCoachDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.<CoachDetailResponse>builder()
                .data(coachService.getCoachDetail(id))
                .build();
    }

    @GetMapping("/featured")
    public ApiResponse<List<CoachResponse>> featured() {

        return ApiResponse.<List<CoachResponse>>builder()
                .data(coachService.getFeaturedCoaches())
                .build();
    }

    @GetMapping("/trending")
    public ApiResponse<List<CoachResponse>> trending() {

        return ApiResponse.<List<CoachResponse>>builder()
                .data(coachService.getTrendingCoaches())
                .build();
    }

    @GetMapping("/{id}/schedule")
    public ApiResponse<List<CoachScheduleResponse>> getSchedule(
            @PathVariable Long id
    ) {
        return ApiResponse.<List<CoachScheduleResponse>>builder()
                .data(coachService.getCoachSchedule(id))
                .build();
    }
    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    public ApiResponse<CoachResponse> createCoach(
            @ModelAttribute CreateCoachRequest request
    ) {
        return ApiResponse.<CoachResponse>builder()
                .data(coachService.createCoach(request))
                .build();
    }
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ApiResponse<CoachResponse> updateCoach(
            @PathVariable Long id,
            @ModelAttribute UpdateCoachRequest request
    ) {

        return ApiResponse.<CoachResponse>builder()
                .data(coachService.updateCoach(id, request))
                .build();
    }

    @PostMapping("/review")
    public ApiResponse<ReviewResponse> createReview(@RequestBody CreateReviewRequest request) {

        return ApiResponse.<ReviewResponse>builder()
                .data(coachService.createReview(request))
                .build();
    }

    @PostMapping("/create/specialization")
    public ApiResponse<SpecializationResponse> create(
            @RequestBody CreateSpecializationRequest request) {

        return ApiResponse.<SpecializationResponse>builder()
                .data(coachService.create(request))
                .build();
    }

    @PostMapping("/create/certificate")
    public ApiResponse<CertificateResponse> createCertificate(
            @RequestBody CreateCertificateRequest request) {

        return ApiResponse.<CertificateResponse>builder()
                .data(coachService.createCertificate(request))
                .build();
    }

    @PostMapping("/create/Schedule")
    public ApiResponse<ScheduleResponse> createSchedule(
            @RequestBody CreateScheduleRequest request) {

        return ApiResponse.<ScheduleResponse>builder()
                .data(coachService.createSchedule(request))
                .build();
    }

}
