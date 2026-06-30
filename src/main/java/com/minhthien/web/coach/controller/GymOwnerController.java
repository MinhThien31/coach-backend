package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.GymOwnerRequests;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.GymOwnerResponses;
import com.minhthien.web.coach.dto.response.WalletHistoryItemResponse;
import com.minhthien.web.coach.dto.response.WalletResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.GymOwnerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gym-owner")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GymOwnerController {

    private final GymOwnerService gymOwnerService;

    @GetMapping("/overview")
    public ApiResponse<GymOwnerResponses.GymOverviewResponse> getOverview(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(gymOwnerService.getOverview(currentUser.getId()));
    }

    @GetMapping("/profile")
    public ApiResponse<GymOwnerResponses.GymProfileResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(gymOwnerService.getProfile(currentUser.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<GymOwnerResponses.GymProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody GymOwnerRequests.GymProfileUpdateRequest request
    ) {
        return ApiResponse.success(gymOwnerService.updateProfile(currentUser.getId(), request));
    }

    @GetMapping("/coaches")
    public ApiResponse<List<GymOwnerResponses.GymCoachResponse>> getCoaches(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(gymOwnerService.getCoaches(currentUser.getId()));
    }

    @PostMapping("/coaches")
    public ApiResponse<GymOwnerResponses.GymCoachResponse> addCoach(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody GymOwnerRequests.GymCoachAddRequest request
    ) {
        return ApiResponse.success(gymOwnerService.addCoach(currentUser.getId(), request));
    }

    @PatchMapping("/coaches/{coachProfileId}/remove")
    public ApiResponse<GymOwnerResponses.GymCoachResponse> removeCoach(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long coachProfileId
    ) {
        return ApiResponse.success(gymOwnerService.removeCoach(currentUser.getId(), coachProfileId));
    }

    @GetMapping("/bookings")
    public ApiResponse<List<GymOwnerResponses.GymBookingResponse>> getBookings(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(gymOwnerService.getBookings(currentUser.getId()));
    }

    @GetMapping("/wallet")
    public ApiResponse<WalletResponse> getWallet(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(gymOwnerService.getWallet(currentUser.getId()));
    }

    @GetMapping("/transactions")
    public ApiResponse<Page<WalletHistoryItemResponse>> getTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(gymOwnerService.getTransactions(currentUser.getId(), page, size));
    }
}
