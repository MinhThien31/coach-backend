package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private Boolean active;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
