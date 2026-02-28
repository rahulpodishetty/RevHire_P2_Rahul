package com.rev.app.mapper;

import com.rev.app.dto.NotificationDto;
import com.rev.app.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        if (notification == null)
            return null;
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
