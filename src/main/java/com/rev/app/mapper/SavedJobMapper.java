package com.rev.app.mapper;

import com.rev.app.dto.SavedJobDto;
import com.rev.app.entity.SavedJob;
import org.springframework.stereotype.Component;

@Component
public class SavedJobMapper {
    private final JobMapper jobMapper;

    public SavedJobMapper(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    public SavedJobDto toDto(SavedJob savedJob) {
        if (savedJob == null)
            return null;
        return SavedJobDto.builder()
                .id(savedJob.getId())
                .jobSeekerId(savedJob.getJobSeeker() != null ? savedJob.getJobSeeker().getId() : null)
                .job(jobMapper.toDto(savedJob.getJob()))
                .savedOn(savedJob.getSavedOn())
                .build();
    }
}
