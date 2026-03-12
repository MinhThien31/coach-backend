package com.minhthien.web.coach.service;

import com.minhthien.web.coach.entity.Video;
import com.minhthien.web.coach.enums.VideoType;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    Video uploadVideo(String title, VideoType type, MultipartFile file);
}
