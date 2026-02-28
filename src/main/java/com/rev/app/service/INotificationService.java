package com.rev.app.service;

import com.rev.app.dto.NotificationDto;
import java.util.List;

public interface INotificationService {
    List<NotificationDto> getUserNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);
}

