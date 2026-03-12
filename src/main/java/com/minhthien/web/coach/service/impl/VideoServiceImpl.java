package com.minhthien.web.coach.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.entity.Video;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.repository.VideoRepository;
import com.minhthien.web.coach.service.ImageService;
import com.minhthien.web.coach.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final Cloudinary cloudinary;
    private final VideoRepository videoRepository;
    @Override
    public Video uploadVideo(String title, VideoType type, MultipartFile file) {

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String url = uploadResult.get("secure_url").toString();

            Video video = Video.builder()
                    .title(title)
                    .url(url)
                    .type(type)
                    .build();

            return videoRepository.save(video);

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed");
        }
    }
}
