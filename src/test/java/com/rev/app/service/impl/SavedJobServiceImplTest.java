package com.rev.app.service.impl;

import com.rev.app.dto.SavedJobDto;
import com.rev.app.entity.Job;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.SavedJob;

import com.rev.app.mapper.SavedJobMapper;
import com.rev.app.repository.IJobRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.ISavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceImplTest {

    @Mock
    private ISavedJobRepository savedJobRepository;
    @Mock
    private IJobRepository jobRepository;
    @Mock
    private IJobSeekerRepository jobSeekerRepository;
    @Mock
    private SavedJobMapper savedJobMapper;

    @InjectMocks
    private SavedJobServiceImpl savedJobService;

    private JobSeeker testSeeker;
    private Job testJob;
    private SavedJob testSavedJob;

    @BeforeEach
    void setUp() {
        testSeeker = new JobSeeker();
        testSeeker.setId(1L);

        testJob = new Job();
        testJob.setId(10L);

        testSavedJob = new SavedJob();
        testSavedJob.setId(100L);
        testSavedJob.setJobSeeker(testSeeker);
        testSavedJob.setJob(testJob);
    }

    @Test
    void saveJob_Success() {
        when(savedJobRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(false);
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(testJob));
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(testSavedJob);
        when(savedJobMapper.toDto(any(SavedJob.class))).thenReturn(new SavedJobDto());

        SavedJobDto result = savedJobService.saveJob(1L, 10L);

        assertNotNull(result);
        verify(savedJobRepository, times(1)).save(any(SavedJob.class));
    }

    @Test
    void saveJob_AlreadySaved() {
        when(savedJobRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(true);

        SavedJobDto result = savedJobService.saveJob(1L, 10L);

        assertNull(result);
        verify(savedJobRepository, never()).save(any(SavedJob.class));
    }

    @Test
    void unsaveJob_Success() {
        savedJobService.unsaveJob(1L, 10L);

        verify(savedJobRepository, times(1)).deleteByJobSeekerIdAndJobId(1L, 10L);
    }

    @Test
    void getSavedJobsBySeeker_Success() {
        when(savedJobRepository.findByJobSeekerId(1L)).thenReturn(List.of(testSavedJob));
        when(savedJobMapper.toDto(any(SavedJob.class))).thenReturn(new SavedJobDto());

        List<SavedJobDto> result = savedJobService.getSavedJobsBySeeker(1L);

        assertEquals(1, result.size());
    }

    @Test
    void isJobSaved_True() {
        when(savedJobRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(true);

        assertTrue(savedJobService.isJobSaved(1L, 10L));
    }

    @Test
    void isJobSaved_False() {
        when(savedJobRepository.existsByJobSeekerIdAndJobId(1L, 10L)).thenReturn(false);

        assertFalse(savedJobService.isJobSaved(1L, 10L));
    }
}
