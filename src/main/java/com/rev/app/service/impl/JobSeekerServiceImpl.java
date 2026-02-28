package com.rev.app.service.impl;

import com.rev.app.dto.JobSeekerDto;
import com.rev.app.entity.JobSeeker;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobSeekerMapper;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.ISavedJobRepository;
import com.rev.app.service.IJobSeekerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobSeekerServiceImpl implements IJobSeekerService {

    private final IJobSeekerRepository IJobSeekerRepository;
    private final IApplicationRepository IApplicationRepository;
    private final ISavedJobRepository ISavedJobRepository;
    private final JobSeekerMapper jobSeekerMapper;

    public JobSeekerServiceImpl(IJobSeekerRepository IJobSeekerRepository,
            IApplicationRepository IApplicationRepository,
            ISavedJobRepository ISavedJobRepository,
            JobSeekerMapper jobSeekerMapper) {
        this.IJobSeekerRepository = IJobSeekerRepository;
        this.IApplicationRepository = IApplicationRepository;
        this.ISavedJobRepository = ISavedJobRepository;
        this.jobSeekerMapper = jobSeekerMapper;
    }

    @Override
    public JobSeekerDto getProfileByUserId(Long userId) {
        JobSeeker jobSeeker = IJobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found for User ID: " + userId));
        return jobSeekerMapper.toDto(jobSeeker);
    }

    @Override
    @Transactional
    public JobSeekerDto updateProfile(Long userId, JobSeekerDto dto) {
        JobSeeker jobSeeker = IJobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found for User ID: " + userId));

        jobSeeker.setName(dto.getName() != null ? dto.getName() : jobSeeker.getName());
        jobSeeker.setPhone(dto.getPhone() != null ? dto.getPhone() : jobSeeker.getPhone());
        jobSeeker.setExperienceYears(
                dto.getExperienceYears() != null ? dto.getExperienceYears() : jobSeeker.getExperienceYears());

        JobSeeker updated = IJobSeekerRepository.save(jobSeeker);
        return jobSeekerMapper.toDto(updated);
    }

    @Override
    public Map<String, Object> getDashboardStats(Long userId) {
        JobSeeker profile = IJobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", IApplicationRepository.countByJobSeekerId(profile.getId()));
        stats.put("savedJobsCount", ISavedJobRepository.countByJobSeekerId(profile.getId()));
        stats.put("notificationsCount", 0); // Placeholder
        return stats;
    }
}
