package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.WebsiteFeedbackRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.WebsiteFeedbackResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.service.WebsiteFeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WebsiteFeedbackController {

    private final WebsiteFeedbackService websiteFeedbackService;

    @GetMapping("/api/v1/website-feedback/me")
    public ResponseEntity<ApiResponse<WebsiteFeedbackResponse>> getMine(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(websiteFeedbackService.getMine(currentUser.getId())));
    }

    @PutMapping("/api/v1/website-feedback/me")
    public ResponseEntity<ApiResponse<WebsiteFeedbackResponse>> saveMine(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WebsiteFeedbackRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Website feedback saved successfully",
                websiteFeedbackService.saveMine(currentUser.getId(), request)
        ));
    }

    @GetMapping("/api/v1/admin/website-feedback")
    public ResponseEntity<ApiResponse<Page<WebsiteFeedbackResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                websiteFeedbackService.getAll(keyword, rating, role, from, to, page, size)
        ));
    }
}
