package com.rev.app.service.impl;

import com.rev.app.dto.JobDto;
import com.rev.app.entity.Company;
import com.rev.app.entity.Employer;
import com.rev.app.entity.Job;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobMapper;
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
class JobServiceImplTest {

    @Mock
    private IJobRepository jobRepository;
    @Mock
    private IEmployerRepository employerRepository;
    @Mock
    private IJobSeekerRepository jobSeekerRepository;
    @Mock
    private INotificationRepository notificationRepository;
    @Mock
    private IApplicationRepository applicationRepository;
    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private Employer testEmployer;
    private Company testCompany;
    private Job testJob;
    private JobDto testJobDto;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Tech Corp");

        testEmployer = new Employer();
        testEmployer.setId(1L);
        testEmployer.setCompany(testCompany);

        testJob = new Job();
        testJob.setId(10L);
        testJob.setCompany(testCompany);
        testJob.setEmployer(testEmployer);
        testJob.setTitle("Developer");
        testJob.setIsDeleted(false);

        testJobDto = new JobDto();
        testJobDto.setTitle("Developer");
    }

    @Test
    void createJob_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(testEmployer));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toDto(any(Job.class))).thenReturn(testJobDto);

        JobDto result = jobService.createJob(1L, testJobDto);

        assertNotNull(result);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void getJobById_Success() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));
        when(jobMapper.toDto(testJob)).thenReturn(testJobDto);

        JobDto result = jobService.getJobById(10L);

        assertNotNull(result);
        assertEquals("Developer", result.getTitle());
    }

    @Test
    void getJobById_NotFound() {
        when(jobRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getJobById(10L));
    }

    @Test
    void deleteJob_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(testEmployer));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));

        jobService.deleteJob(1L, 10L);

        assertTrue(testJob.getIsDeleted());
        verify(jobRepository, times(1)).save(testJob);
    }

    @Test
    void updateJob_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(testEmployer));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toDto(any(Job.class))).thenReturn(testJobDto);

        JobDto updatedDto = new JobDto();
        updatedDto.setTitle("Senior Developer");

        JobDto result = jobService.updateJob(1L, 10L, updatedDto);

        assertNotNull(result);
        verify(jobRepository, times(1)).save(any(Job.class));
    }
}
