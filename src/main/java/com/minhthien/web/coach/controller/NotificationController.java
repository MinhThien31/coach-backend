package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.NotificationResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(notificationService.getNotifications(currentUser.getId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(Map.of("unreadCount", notificationService.getUnreadCount(currentUser.getId())));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ApiResponse.success(notificationService.markRead(currentUser.getId(), id));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllRead(currentUser.getId());
        return ApiResponse.success("Notifications marked as read", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        notificationService.deleteNotification(currentUser.getId(), id);
        return ApiResponse.success("Notification deleted successfully", null);
    }
}
