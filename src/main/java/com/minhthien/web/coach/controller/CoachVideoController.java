package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.service.CoachVideoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/coach/videos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CoachVideoController {

    private final CoachVideoService coachVideoService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public CoachVideo uploadVideo(
            @RequestParam Long coachId,
            @RequestParam String title,
            @RequestParam String format,
            @RequestParam String resolution,
            @RequestParam List<String> tags,
            @RequestParam MultipartFile file
    ) {

        return coachVideoService.uploadVideo(
                coachId,
                title,
                format,
                resolution,
                tags,
                file
        );
    }
}
