package com.rev.app.mapper;

import com.rev.app.dto.JobDto;
import com.rev.app.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
    private final CompanyMapper companyMapper;

    public JobMapper(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    public JobDto toDto(Job job) {
        if (job == null)
            return null;
        return JobDto.builder()
                .id(job.getId())
                .company(companyMapper.toDto(job.getCompany()))
                .title(job.getTitle())
                .description(job.getDescription())
                .skillsRequired(job.getSkillsRequired())
                .experienceRequired(job.getExperienceRequired())
                .educationRequired(job.getEducationRequired())
                .location(job.getLocation())
                .salaryRange(job.getSalaryRange())
                .jobType(job.getJobType())
                .deadline(job.getDeadline())
                .build();
    }
}
