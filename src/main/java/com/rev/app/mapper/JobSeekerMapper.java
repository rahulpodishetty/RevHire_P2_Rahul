package com.rev.app.mapper;

import com.rev.app.dto.JobSeekerDto;
import com.rev.app.entity.JobSeeker;
import org.springframework.stereotype.Component;

@Component
public class JobSeekerMapper {
    public JobSeekerDto toDto(JobSeeker jobSeeker) {
        if (jobSeeker == null)
            return null;
        return JobSeekerDto.builder()
                .id(jobSeeker.getId())
                .userId(jobSeeker.getUser() != null ? jobSeeker.getUser().getId() : null)
                .name(jobSeeker.getName())
                .phone(jobSeeker.getPhone())
                .experienceYears(jobSeeker.getExperienceYears())
                .profileCompletion(jobSeeker.getProfileCompletion())
                .build();
    }
}
