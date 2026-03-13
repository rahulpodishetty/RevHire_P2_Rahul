package com.rev.app.mapper;

import com.rev.app.dto.JobDto;
import com.rev.app.entity.Job;
import com.rev.app.repository.IJobRepository;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
    private final CompanyMapper companyMapper;
    private final IJobRepository jobRepository;

    public JobMapper(CompanyMapper companyMapper, IJobRepository jobRepository) {
        this.companyMapper = companyMapper;
        this.jobRepository = jobRepository;
    }

    public JobDto toDto(Job job) {
        if (job == null)
            return null;
        // Use a safe COUNT query instead of accessing the lazy applications collection
        long applicantCount = (job.getId() != null)
                ? jobRepository.countApplicationsByJobId(job.getId())
                : 0L;
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
                .openings(job.getOpenings())
                .status(job.getStatus())
                .postedDate(job.getPostedDate())
                .applicantCount(applicantCount)
                .build();
    }
}
