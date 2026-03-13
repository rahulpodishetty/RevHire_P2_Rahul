package com.rev.app.service;

import com.rev.app.dto.NotificationDto;
import java.util.List;

public interface INotificationService {
    List<NotificationDto> getUserNotifications(Long userId);

    long getUnreadCount(Long userId);

    List<NotificationDto> getUnreadNotifications(Long userId);

    org.springframework.data.domain.Page<NotificationDto> getPaginatedNotifications(Long userId,
            org.springframework.data.domain.Pageable pageable);

    void markAsRead(Long notificationId, Long userId);
}
