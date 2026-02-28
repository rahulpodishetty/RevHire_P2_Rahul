package com.rev.app.service.impl;

import com.rev.app.dto.JobDto;
import com.rev.app.entity.Employer;
import com.rev.app.entity.Job;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobMapper;
import com.rev.app.repository.IEmployerRepository;
import com.rev.app.repository.IJobRepository;
import com.rev.app.service.IJobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobServiceImpl implements IJobService {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    private final IJobRepository IJobRepository;
    private final IEmployerRepository IEmployerRepository;
    private final JobMapper jobMapper;

    public JobServiceImpl(IJobRepository IJobRepository, IEmployerRepository IEmployerRepository, JobMapper jobMapper) {
        this.IJobRepository = IJobRepository;
        this.IEmployerRepository = IEmployerRepository;
        this.jobMapper = jobMapper;
    }

    @Override
    @Transactional
    public JobDto createJob(Long employerId, JobDto dto) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = new Job();
        job.setCompany(employer.getCompany());
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setEducationRequired(dto.getEducationRequired());
        job.setLocation(dto.getLocation());
        job.setSalaryRange(dto.getSalaryRange());
        job.setJobType(dto.getJobType());
        job.setDeadline(dto.getDeadline());

        return jobMapper.toDto(IJobRepository.save(job));
    }

    @Override
    @Transactional
    public JobDto updateJob(Long employerId, Long jobId, JobDto dto) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = IJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("You don't own this job.");
        }

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setEducationRequired(dto.getEducationRequired());
        job.setLocation(dto.getLocation());
        job.setSalaryRange(dto.getSalaryRange());
        job.setJobType(dto.getJobType());
        job.setDeadline(dto.getDeadline());

        return jobMapper.toDto(IJobRepository.save(job));
    }

    @Override
    public JobDto getJobById(Long jobId) {
        return jobMapper.toDto(IJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found")));
    }

    @Override
    public Page<JobDto> getAllJobs(String title, String location, Integer experience, String company, String salary,
            String jobType, Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (location != null && !location.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (experience != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceRequired"), experience));
            }
            if (company != null && !company.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.join("company").get("name")), "%" + company.toLowerCase() + "%"));
            }
            if (jobType != null && !jobType.isEmpty()) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return IJobRepository.findAll(spec, pageable).map(jobMapper::toDto);
    }

    @Override
    public Page<JobDto> getJobsByEmployer(Long employerId, Pageable pageable) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        return IJobRepository.findByCompanyId(employer.getCompany().getId(), pageable).map(jobMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteJob(Long employerId, Long jobId) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Job job = IJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("You don't own this job.");
        }

        IJobRepository.delete(job);
    }
}
