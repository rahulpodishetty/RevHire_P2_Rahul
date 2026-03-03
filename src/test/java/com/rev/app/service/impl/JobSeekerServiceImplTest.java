package com.rev.app.service.impl;

import com.rev.app.dto.JobSeekerDto;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobSeekerMapper;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.INotificationRepository;
import com.rev.app.repository.ISavedJobRepository;
import com.rev.app.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSeekerServiceImplTest {

    @Mock
    private IJobSeekerRepository jobSeekerRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IApplicationRepository applicationRepository;

    @Mock
    private ISavedJobRepository savedJobRepository;

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private JobSeekerMapper jobSeekerMapper;

    @InjectMocks
    private JobSeekerServiceImpl jobSeekerService;

    @Test
    void testGetProfileByUserId_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setId(10L);
        jobSeeker.setUser(user);
        jobSeeker.setName("Test Name");

        JobSeekerDto dto = new JobSeekerDto();
        dto.setId(10L);
        dto.setName("Test Name");

        when(jobSeekerRepository.findByUserId(userId)).thenReturn(Optional.of(jobSeeker));
        when(jobSeekerMapper.toDto(jobSeeker)).thenReturn(dto);

        JobSeekerDto result = jobSeekerService.getProfileByUserId(userId);

        assertNotNull(result);
        assertEquals("Test Name", result.getName());
        verify(jobSeekerRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetProfileByUserId_NotFound_CreatesStub() {
        // When JobSeeker profile doesn't exist, the service creates a stub
        // by looking up the User. If User also doesn't exist, ResourceNotFoundException
        // is thrown.
        Long userId = 1L;
        when(jobSeekerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobSeekerService.getProfileByUserId(userId));
    }

    @Test
    void testGetProfileByUserId_NotFound_CreatesStubSuccessfully() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        JobSeeker savedSeeker = new JobSeeker();
        savedSeeker.setId(10L);
        savedSeeker.setUser(user);
        savedSeeker.setName("test");

        JobSeekerDto dto = new JobSeekerDto();
        dto.setId(10L);
        dto.setName("test");

        when(jobSeekerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jobSeekerRepository.save(any(JobSeeker.class))).thenReturn(savedSeeker);
        when(jobSeekerMapper.toDto(savedSeeker)).thenReturn(dto);

        JobSeekerDto result = jobSeekerService.getProfileByUserId(userId);

        assertNotNull(result);
        assertEquals("test", result.getName());
        verify(jobSeekerRepository, times(1)).save(any(JobSeeker.class));
    }

    @Test
    void testUpdateProfile_NotFound() {
        Long userId = 1L;
        when(jobSeekerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.updateProfile(userId, new JobSeekerDto()));
    }
}
