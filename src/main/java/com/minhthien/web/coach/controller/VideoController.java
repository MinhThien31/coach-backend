package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.entity.Video;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.service.VideoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Video uploadVideo(
            @RequestParam String title,
            @RequestParam VideoType type,
            @RequestParam MultipartFile file
    ) {

        return videoService.uploadVideo(title, type, file);
    }
}
