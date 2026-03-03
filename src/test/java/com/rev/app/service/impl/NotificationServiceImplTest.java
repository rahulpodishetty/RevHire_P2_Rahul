package com.rev.app.service.impl;

import com.rev.app.dto.NotificationDto;
import com.rev.app.entity.Notification;
import com.rev.app.entity.User;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.NotificationMapper;
import com.rev.app.repository.INotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification;
    private NotificationDto testNotificationDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testNotification = new Notification();
        testNotification.setId(10L);
        testNotification.setUser(testUser);
        testNotification.setIsRead(false);

        testNotificationDto = new NotificationDto();
        testNotificationDto.setId(10L);
    }

    @Test
    void getUserNotifications_Success() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testNotification));
        when(notificationMapper.toDto(testNotification)).thenReturn(testNotificationDto);

        List<NotificationDto> result = notificationService.getUserNotifications(1L);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByUserIdAndIsReadFalse(1L);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(10L, 1L);

        assertTrue(testNotification.getIsRead());
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    void markAsRead_NotFound() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(10L, 1L));
    }

    @Test
    void markAsRead_AccessDenied() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(testNotification));

        assertThrows(CustomAccessDeniedException.class, () -> notificationService.markAsRead(10L, 2L));
    }
}
