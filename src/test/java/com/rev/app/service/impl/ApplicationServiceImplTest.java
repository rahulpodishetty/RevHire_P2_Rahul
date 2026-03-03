package com.rev.app.service.impl;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.entity.*;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.mapper.ApplicationMapper;
import com.rev.app.mapper.ApplicationNoteMapper;
import com.rev.app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private IApplicationRepository applicationRepository;
    @Mock
    private IJobRepository jobRepository;
    @Mock
    private IJobSeekerRepository jobSeekerRepository;
    @Mock
    private IEmployerRepository employerRepository;
    @Mock
    private IResumeRepository resumeRepository;
    @Mock
    private IApplicationNoteRepository applicationNoteRepository;
    @Mock
    private INotificationRepository notificationRepository;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private ApplicationNoteMapper applicationNoteMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private JobSeeker testSeeker;
    private Job testJob;
    private Resume testResume;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        User seekerUser = new User();
        seekerUser.setId(100L);

        User employerUser = new User();
        employerUser.setId(200L);

        Employer employer = new Employer();
        employer.setId(50L);
        employer.setUser(employerUser);

        testSeeker = new JobSeeker();
        testSeeker.setId(1L);
        testSeeker.setName("John Doe");
        testSeeker.setUser(seekerUser);

        testJob = new Job();
        testJob.setId(10L);
        testJob.setTitle("Software Engineer");
        testJob.setStatus("ACTIVE");
        testJob.setEmployer(employer);

        testResume = new Resume();
        testResume.setId(100L);
        testResume.setJobSeeker(testSeeker);

        testApplication = new Application();
        testApplication.setId(500L);
        testApplication.setJobSeeker(testSeeker);
        testApplication.setJob(testJob);
    }

    @Test
    void applyToJob_Success() {
        when(applicationRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(false);
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(testResume));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        when(applicationMapper.toDto(any(Application.class))).thenReturn(new ApplicationDto());

        ApplicationDto result = applicationService.applyToJob(1L, 10L, 100L, "Cover letter");

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void applyToJob_AlreadyApplied() {
        when(applicationRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            applicationService.applyToJob(1L, 10L, 100L, "Cover letter"));
    }

    @Test
    void applyToJob_JobInactive() {
        testJob.setStatus("CLOSED");
        when(applicationRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(false);
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyToJob(1L, 10L, 100L, "Cover letter"));
    }

    @Test
    void withdrawApplication_Success() {
        when(applicationRepository.findById(500L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        when(applicationMapper.toDto(any(Application.class))).thenReturn(new ApplicationDto());

        applicationService.withdrawApplication(1L, 500L, "Moving");

        assertEquals("Withdrawn", testApplication.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void updateApplicationStatus_Success() {
        Employer employer = new Employer();
        Company company = new Company();
        company.setId(5L);
        employer.setCompany(company);
        testJob.setCompany(company);

        when(employerRepository.findById(2L)).thenReturn(Optional.of(employer));
        when(applicationRepository.findById(500L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        when(applicationMapper.toDto(any(Application.class))).thenReturn(new ApplicationDto());

        applicationService.updateApplicationStatus(2L, 500L, "Shortlisted");

        assertEquals("Shortlisted", testApplication.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void updateApplicationStatus_Unauthorized() {
        Employer employer = new Employer();
        Company company = new Company();
        company.setId(5L);
        employer.setCompany(company);

        Company jobCompany = new Company();
        jobCompany.setId(6L);
        testJob.setCompany(jobCompany);

        when(employerRepository.findById(2L)).thenReturn(Optional.of(employer));
        when(applicationRepository.findById(500L)).thenReturn(Optional.of(testApplication));

        assertThrows(CustomAccessDeniedException.class,
                () -> applicationService.updateApplicationStatus(2L, 500L, "Shortlisted"));
    }
}
