package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.LoginRequest;
import com.minhthien.web.coach.dto.request.RegisterRequest;
import com.minhthien.web.coach.dto.response.AuthResponse;
import com.minhthien.web.coach.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);

    AuthResponse loginGoogle(String idToken);

}
