package com.rev.app.service.impl;

import com.rev.app.dto.NotificationDto;
import com.rev.app.entity.Notification;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.NotificationMapper;
import com.rev.app.repository.INotificationRepository;
import com.rev.app.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository INotificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(INotificationRepository INotificationRepository,
            NotificationMapper notificationMapper) {
        this.INotificationRepository = INotificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<NotificationDto> getUserNotifications(Long userId) {
        return INotificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return INotificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return INotificationRepository.findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<NotificationDto> getPaginatedNotifications(Long userId,
            org.springframework.data.domain.Pageable pageable) {
        return INotificationRepository.findByUserId(userId, pageable).map(notificationMapper::toDto);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = INotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomAccessDeniedException("Not your notification");
        }
        notification.setIsRead(true);
        INotificationRepository.save(notification);
    }
}
