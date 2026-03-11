package com.minhthien.web.coach.service.impl;


import com.minhthien.web.coach.dto.request.CreateTraineeRequest;
import com.minhthien.web.coach.dto.request.UpdateTraineeRequest;
import com.minhthien.web.coach.dto.response.TraineeResponse;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.TraineeProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.TraineeProfileRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.ImageService;
import com.minhthien.web.coach.service.TraineeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeProfileRepository traineeProfileRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final BookingRepository bookingRepository;
    private final CoachRepository coachRepository;


    @Override
    public TraineeResponse createTrainee(CreateTraineeRequest request) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        String avatarUrl = imageService.upload(request.getAvatar());

        TraineeProfile trainee = new TraineeProfile();

        trainee.setUser(user);
        trainee.setGoal(request.getGoal());
        trainee.setAge(request.getAge());
        trainee.setWeight(request.getWeight());
        trainee.setHeight(request.getHeight());
        trainee.setPhone(request.getPhone());
        trainee.setAvatar(avatarUrl);

        traineeProfileRepository.save(trainee);

        return TraineeResponse.builder()
                .id(trainee.getId())
                .fullName(user.getFullName())
                .avatar(avatarUrl)
                .goal(trainee.getGoal())
                .age(trainee.getAge())
                .weight(trainee.getWeight())
                .height(trainee.getHeight())
                .phone(trainee.getPhone())
                .build();
    }

    @Override
    public TraineeResponse updateTrainee(Long id, UpdateTraineeRequest request) {
        TraineeProfile trainee = traineeProfileRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));

        if (request.getGoal() != null) {
            trainee.setGoal(request.getGoal());
        }

        if (request.getAge() != null) {
            trainee.setAge(request.getAge());
        }

        if (request.getWeight() != null) {
            trainee.setWeight(request.getWeight());
        }

        if (request.getHeight() != null) {
            trainee.setHeight(request.getHeight());
        }

        if (request.getPhone() != null) {
            trainee.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {

            String avatarUrl = imageService.upload(request.getAvatar());

            trainee.setAvatar(avatarUrl);
        }

        traineeProfileRepository.save(trainee);

        return TraineeResponse.builder()
                .id(trainee.getId())
                .fullName(trainee.getUser().getFullName())
                .avatar(trainee.getAvatar())
                .goal(trainee.getGoal())
                .age(trainee.getAge())
                .weight(trainee.getWeight())
                .height(trainee.getHeight())
                .phone(trainee.getPhone())
                .build();
    }

    @Override
    public void deleteTrainee(Long id) {
        TraineeProfile trainee = traineeProfileRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));

        traineeProfileRepository.delete(trainee);
    }

    @Override
    public List<TraineeResponse> searchMyTrainees(String keyword) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        CoachProfile coach = coachRepository
                .findByUserId(user.getId())
                .orElseThrow();

        List<TraineeProfile> trainees =
                bookingRepository.searchBookedTrainees(coach.getId(), keyword);

        return trainees.stream()
                .map(t -> TraineeResponse.builder()
                        .id(t.getId())
                        .fullName(t.getUser().getFullName())
                        .avatar(t.getAvatar())
                        .goal(t.getGoal())
                        .age(t.getAge())
                        .weight(t.getWeight())
                        .height(t.getHeight())
                        .phone(t.getPhone())
                        .build())
                .toList();
    }


}
