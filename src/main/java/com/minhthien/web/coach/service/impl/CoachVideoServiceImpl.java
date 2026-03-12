package com.minhthien.web.coach.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.entity.Category;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.CoachVideo;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.CoachVideoRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.CoachVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoachVideoServiceImpl implements CoachVideoService {
    private final Cloudinary cloudinary;
    private final CoachVideoRepository coachVideoRepository;
    private final UserRepository userRepository;
    private final CoachRepository coachRepository;


    @Override
    public CoachVideo uploadVideo(Long coachId, String title, String format, String resolution, List<String> tags, MultipartFile file) {
        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String videoUrl = uploadResult.get("secure_url").toString();

            double sizeGB = file.getSize() / (1024.0 * 1024 * 1024);

            User coach = userRepository.findById(coachId)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));

            CoachProfile coachProfile = coachRepository
                    .findByUserId(coachId)
                    .orElseThrow(() -> new RuntimeException("Coach profile not found"));

            Category category = coachProfile.getCategory();

            CoachVideo video = CoachVideo.builder()
                    .title(title)
                    .videoUrl(videoUrl)
                    .format(format)
                    .resolution(resolution)
                    .size(sizeGB)
                    .uploadDate(LocalDate.now())
                    .tags(String.join(",", tags))
                    .category(category)
                    .coach(coach)
                    .build();

            return coachVideoRepository.save(video);

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed");
        }
    }
}
