package com.minhthien.web.coach.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.minhthien.web.coach.dto.request.LoginRequest;
import com.minhthien.web.coach.dto.request.RegisterRequest;
import com.minhthien.web.coach.dto.response.AuthResponse;
import com.minhthien.web.coach.dto.response.UserResponse;
import com.minhthien.web.coach.entity.PasswordResetOtp;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.DuplicateResourceException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.PasswordResetOtpRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.security.JwtTokenProvider;
import com.minhthien.web.coach.service.AuthService;
import com.minhthien.web.coach.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private MailService mailService;
    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .Phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String usernameOrEmail) {

        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() ->
                        userRepository.findByEmail(usernameOrEmail)
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "User not found: " + usernameOrEmail)));

        return mapToUserResponse(user);
    }

    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(resetOtp);

        mailService.sendOtp(email, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        PasswordResetOtp resetOtp = otpRepository
                .findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (resetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

    }

    @Override
    public AuthResponse loginGoogle(String idToken) {

        try {

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleToken = verifier.verify(idToken);

            if (googleToken == null) {
                throw new BadRequestException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {

                        User newUser = User.builder()
                                .email(email)
                                .username(name)
                                .avatarUrl(picture)
                                .role(UserRole.TRAINEES)
                                .active(true)
                                .build();

                        return userRepository.save(newUser);
                    });

            String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {

            throw new RuntimeException("Google login failed");
        }
    }

    @Override
    public AuthResponse loginFacebook(String accessToken) {
        try {

            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            String email = (String) response.get("email");
            String name = (String) response.get("name");
            String id = (String) response.get("id");

            if(email == null){
                email = id + "@facebook.com";
            }

            final String finalEmail = email;

            User user = userRepository.findByEmail(finalEmail)
                    .orElseGet(() -> {

                        User newUser = User.builder()
                                .email(finalEmail)
                                .username(name)
                                .role(UserRole.TRAINEES)
                                .active(true)
                                .provider("FACEBOOK")
                                .build();

                        return userRepository.save(newUser);
                    });

            String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {

            throw new RuntimeException("Facebook login failed");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
