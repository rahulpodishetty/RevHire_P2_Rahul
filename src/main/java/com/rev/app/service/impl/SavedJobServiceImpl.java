package com.rev.app.service.impl;

import com.rev.app.dto.SavedJobDto;
import com.rev.app.entity.Job;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.SavedJob;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.SavedJobMapper;
import com.rev.app.repository.IJobRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.ISavedJobRepository;
import com.rev.app.service.ISavedJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedJobServiceImpl implements ISavedJobService {

    private final ISavedJobRepository savedJobRepository;
    private final IJobRepository jobRepository;
    private final IJobSeekerRepository jobSeekerRepository;
    private final SavedJobMapper savedJobMapper;

    public SavedJobServiceImpl(ISavedJobRepository savedJobRepository, IJobRepository jobRepository,
            IJobSeekerRepository jobSeekerRepository, SavedJobMapper savedJobMapper) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.savedJobMapper = savedJobMapper;
    }

    @Override
    @Transactional
    public SavedJobDto saveJob(Long jobSeekerId, Long jobId) {
        if (jobSeekerId == null || jobId == null) {
            throw new IllegalArgumentException("Job Seeker ID and Job ID must not be null");
        }

        if (savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId)) {
            System.out.println("DEBUG: Job " + jobId + " already saved by Seeker " + jobSeekerId);
            return null;
        }

        JobSeeker seeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker not found with ID: " + jobSeekerId));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        SavedJob savedJob = SavedJob.builder()
                .jobSeeker(seeker)
                .job(job)
                .build();

        SavedJob saved = savedJobRepository.save(savedJob);
        System.out.println("DEBUG: Successfully saved job " + jobId + " for seeker " + jobSeekerId + " with Saved ID: "
                + saved.getId());
        return savedJobMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void unsaveJob(Long jobSeekerId, Long jobId) {
        savedJobRepository.deleteByJobSeekerIdAndJobId(jobSeekerId, jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedJobDto> getSavedJobsBySeeker(Long jobSeekerId) {
        return savedJobRepository.findByJobSeekerId(jobSeekerId).stream()
                .map(savedJobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isJobSaved(Long jobSeekerId, Long jobId) {
        return savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId);
    }
}
