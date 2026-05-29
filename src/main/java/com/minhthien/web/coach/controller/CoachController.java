package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.*;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.CoachService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return ApiResponse.<List<CoachScheduleResponse>>builder()
                .data(coachService.getCoachSchedule(id, startDate, endDate))
                .build();
    }

    @GetMapping("/{id}/available-slots")
    public ApiResponse<List<CoachScheduleResponse>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam LocalDate date
    ) {
        return ApiResponse.<List<CoachScheduleResponse>>builder()
                .data(coachService.getAvailableSlots(id, date))
                .build();
    }

    @GetMapping("/{id}/schedule-with-availability")
    public ApiResponse<List<CoachScheduleResponse>> getScheduleWithAvailability(
            @PathVariable Long id
    ) {
        return ApiResponse.<List<CoachScheduleResponse>>builder()
                .data(coachService.getScheduleWithAvailability(id))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<CoachResponse> getMyCoachProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        return ApiResponse.<CoachResponse>builder()
                .data(coachService.getMyCoachProfile(currentUser.getId()))
                .build();
    }

    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ApiResponse<CoachResponse> updateMyCoachProfile(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute UpdateCoachRequest request
    ) {
        return ApiResponse.<CoachResponse>builder()
                .data(coachService.updateMyCoachProfile(currentUser.getId(), request))
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

    @PutMapping("/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long scheduleId,
            @RequestBody CreateScheduleRequest request) {

        return ApiResponse.<ScheduleResponse>builder()
                .data(coachService.updateSchedule(currentUser.getId(), scheduleId, request))
                .build();
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long scheduleId) {

        coachService.deleteSchedule(currentUser.getId(), scheduleId);
        return ApiResponse.<Void>builder()
                .message("Schedule deleted successfully")
                .build();
    }

}
