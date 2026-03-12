package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlatformInfoResponse {
    private String platformName;
    private String version;
    private String environment;
    private String timezone;
    private Long totalUsers;
    private Double monthlyUptime;
    private LocalDateTime lastUpdatedAt;
}
