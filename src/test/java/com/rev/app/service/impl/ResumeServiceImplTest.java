package com.rev.app.service.impl;

import com.rev.app.dto.ResumeDto;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.Resume;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.ResumeMapper;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.IResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private IResumeRepository resumeRepository;
    @Mock
    private IJobSeekerRepository jobSeekerRepository;
    @Mock
    private ResumeMapper resumeMapper;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private JobSeeker testSeeker;
    private Resume testResume;
    private ResumeDto testResumeDto;

    @BeforeEach
    void setUp() {
        testSeeker = new JobSeeker();
        testSeeker.setId(1L);

        testResume = new Resume();
        testResume.setId(10L);
        testResume.setJobSeeker(testSeeker);
        testResume.setIsActive(true);

        testResumeDto = new ResumeDto();
        testResumeDto.setId(10L);
    }

    @Test
    void saveResume_NewResume_FirstResume() {
        ResumeDto newDto = new ResumeDto();
        newDto.setObjective("My Objective");

        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Collections.emptyList());
        when(resumeRepository.save(any(Resume.class))).thenReturn(testResume);
        when(resumeMapper.toDto(any(Resume.class))).thenReturn(testResumeDto);

        ResumeDto result = resumeService.saveResume(1L, newDto);

        assertNotNull(result);
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void saveResume_UpdateExisting_Success() {
        ResumeDto updateDto = new ResumeDto();
        updateDto.setId(10L);
        updateDto.setObjective("Updated Objective");

        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(testResume);
        when(resumeMapper.toDto(any(Resume.class))).thenReturn(testResumeDto);

        ResumeDto result = resumeService.saveResume(1L, updateDto);

        assertNotNull(result);
    }

    @Test
    void saveResume_UpdateExisting_Unauthorized() {
        JobSeeker otherSeeker = new JobSeeker();
        otherSeeker.setId(2L);
        testResume.setJobSeeker(otherSeeker);

        ResumeDto updateDto = new ResumeDto();
        updateDto.setId(10L);

        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(testSeeker));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));

        assertThrows(CustomAccessDeniedException.class, () -> resumeService.saveResume(1L, updateDto));
    }

    @Test
    void getResumeById_Success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));
        when(resumeMapper.toDto(testResume)).thenReturn(testResumeDto);

        ResumeDto result = resumeService.getResumeById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getResumeById_NotFound() {
        when(resumeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resumeService.getResumeById(999L));
    }

    @Test
    void deleteResume_Success() {
        testResume.setIsActive(false);
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));

        resumeService.deleteResume(10L, 1L);

        verify(resumeRepository, times(1)).delete(testResume);
    }

    @Test
    void deleteResume_Unauthorized() {
        JobSeeker otherSeeker = new JobSeeker();
        otherSeeker.setId(99L);
        testResume.setJobSeeker(otherSeeker);

        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));

        assertThrows(CustomAccessDeniedException.class, () -> resumeService.deleteResume(10L, 1L));
    }

    @Test
    void getResumesByJobSeeker_Success() {
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(List.of(testResume));
        when(resumeMapper.toDto(any(Resume.class))).thenReturn(testResumeDto);

        List<ResumeDto> result = resumeService.getResumesByJobSeeker(1L);

        assertEquals(1, result.size());
    }
}
