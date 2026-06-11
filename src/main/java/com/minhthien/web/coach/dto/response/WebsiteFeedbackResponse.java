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
public class WebsiteFeedbackResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
