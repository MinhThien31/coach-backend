package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.UpdatePasswordRequest;
import com.minhthien.web.coach.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);
    UserResponse updateUserStatus(Long userId, boolean active);
}
