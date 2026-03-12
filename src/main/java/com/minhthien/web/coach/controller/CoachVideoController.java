package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.VideoDetailResponse;
import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.enums.VideoType;
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
            @RequestParam VideoType videoType,
            @RequestParam MultipartFile file
    ) {

        return coachVideoService.uploadVideo(
                coachId,
                title,
                format,
                resolution,
                tags,
                videoType,
                file
        );
    }
    @GetMapping("/coach/videos")
    public List<CoachVideo> getCoachVideos(
            @RequestParam Long coachId
    ) {
        return coachVideoService.getVideosByCoach(coachId);
    }
    @GetMapping("/coach/videos/{videoId}")
    public VideoDetailResponse getVideoDetail(
            @PathVariable Long videoId
    ) {
        return coachVideoService.getVideoDetail(videoId);
    }

    @GetMapping("/coach/videos/search")
    public List<CoachVideo> searchVideos(

            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) VideoType type,

            @RequestParam(required = false) Long coachId
    ) {

        return coachVideoService.searchVideos(
                keyword,
                type,
                coachId
        );
    }

    @PostMapping("/coach/videos/{videoId}/view")
    public CoachVideo increaseView(
            @PathVariable Long videoId
    ) {
        return coachVideoService.increaseView(videoId);
    }
}
