package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.WebRtcIceServerResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.WebRtcService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webrtc")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WebRtcController {

    private final WebRtcService webRtcService;

    @GetMapping("/ice-servers")
    public ApiResponse<WebRtcIceServerResponse> getIceServers(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(webRtcService.getIceServers(currentUser));
    }
}
