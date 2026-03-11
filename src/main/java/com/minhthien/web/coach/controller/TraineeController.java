package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.CreateTraineeRequest;
import com.minhthien.web.coach.dto.request.UpdateTraineeRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.TraineeResponse;
import com.minhthien.web.coach.service.TraineeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TraineeController {

    private final TraineeService traineeService;

    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    public ApiResponse<TraineeResponse> createTrainee(
            @ModelAttribute CreateTraineeRequest request
    ) {
        return ApiResponse.<TraineeResponse>builder()
                .data(traineeService.createTrainee(request))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ApiResponse<TraineeResponse> updateTrainee(
            @PathVariable Long id,
            @ModelAttribute UpdateTraineeRequest request
    ) {

        return ApiResponse.<TraineeResponse>builder()
                .data(traineeService.updateTrainee(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTrainee(
            @PathVariable Long id
    ) {

        traineeService.deleteTrainee(id);

        return ApiResponse.<Void>builder()
                .message("Trainee deleted successfully")
                .build();
    }

    @GetMapping("/my-trainees/search")
    public ApiResponse<List<TraineeResponse>> searchMyTrainees(
            @RequestParam String keyword
    ) {

        return ApiResponse.<List<TraineeResponse>>builder()
                .data(traineeService.searchMyTrainees(keyword))
                .build();
    }

}