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
                .email(jobSeeker.getUser() != null ? jobSeeker.getUser().getEmail() : null)
                .experienceYears(jobSeeker.getExperienceYears())
                .location(jobSeeker.getLocation())
                .currentEmploymentStatus(jobSeeker.getCurrentEmploymentStatus())
                .education(jobSeeker.getEducation())
                .workExperience(jobSeeker.getWorkExperience())
                .skills(jobSeeker.getSkills())
                .certifications(jobSeeker.getCertifications())
                .projects(jobSeeker.getProjects())
                .profileCompletion(jobSeeker.getProfileCompletion())
                .profileImagePath(jobSeeker.getProfileImagePath())
                .build();
    }
}
