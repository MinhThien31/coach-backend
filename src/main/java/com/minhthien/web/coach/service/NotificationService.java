package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Long userId);

    long getUnreadCount(Long userId);

    NotificationResponse markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);

    void deleteNotification(Long userId, Long notificationId);

    void createNotification(Long userId, String title, String message, String type);
}
