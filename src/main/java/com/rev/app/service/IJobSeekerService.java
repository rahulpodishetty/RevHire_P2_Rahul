package com.rev.app.service;

import com.rev.app.dto.JobSeekerDto;

import java.util.Map;

public interface IJobSeekerService {
    JobSeekerDto getProfileByUserId(Long userId);

    JobSeekerDto updateProfile(Long userId, JobSeekerDto dto);

    Map<String, Object> getDashboardStats(Long jobSeekerId);
}
