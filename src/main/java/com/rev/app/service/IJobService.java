package com.rev.app.service;

import com.rev.app.dto.JobDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IJobService {
    JobDto createJob(Long employerId, JobDto dto);

    JobDto updateJob(Long employerId, Long jobId, JobDto dto);

    JobDto getJobById(Long jobId);

    Page<JobDto> getAllJobs(String title, String location, Integer experience, String company, String salary,
            String jobType, java.time.LocalDate postedDate, org.springframework.data.domain.Pageable pageable);

    Page<JobDto> getJobsByEmployer(Long employerId, String status, Pageable pageable);

    void deleteJob(Long employerId, Long jobId);
}
