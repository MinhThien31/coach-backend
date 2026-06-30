package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.GymOwnerRequests;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.GymOwnerResponses;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.service.GymOwnerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/gyms")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminGymController {

    private final GymOwnerService gymOwnerService;

    @GetMapping
    public ApiResponse<List<GymOwnerResponses.GymProfileResponse>> getGyms(
            @RequestParam(required = false) GymProfileStatus status
    ) {
        return ApiResponse.success(gymOwnerService.getAdminGyms(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<GymOwnerResponses.GymProfileResponse> getGym(@PathVariable Long id) {
        return ApiResponse.success(gymOwnerService.getAdminGym(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<GymOwnerResponses.GymProfileResponse> updateGymStatus(
            @PathVariable Long id,
            @Valid @RequestBody GymOwnerRequests.AdminGymStatusUpdateRequest request
    ) {
        return ApiResponse.success(gymOwnerService.updateAdminGymStatus(id, request));
    }

    @PatchMapping("/{gymId}/coaches/{coachProfileId}/remove")
    public ApiResponse<GymOwnerResponses.GymCoachResponse> removeCoach(
            @PathVariable Long gymId,
            @PathVariable Long coachProfileId
    ) {
        return ApiResponse.success(gymOwnerService.adminRemoveCoach(gymId, coachProfileId));
    }
}
