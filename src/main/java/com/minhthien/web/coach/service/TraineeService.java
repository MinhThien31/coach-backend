package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.CreateTraineeRequest;
import com.minhthien.web.coach.dto.request.UpdateTraineeRequest;
import com.minhthien.web.coach.dto.response.TraineeResponse;

import java.util.List;

public interface TraineeService {

    TraineeResponse createTrainee(CreateTraineeRequest request);

    TraineeResponse updateTrainee(Long id, UpdateTraineeRequest request);

    void deleteTrainee(Long id);
    List<TraineeResponse> searchMyTrainees(String keyword);
}
