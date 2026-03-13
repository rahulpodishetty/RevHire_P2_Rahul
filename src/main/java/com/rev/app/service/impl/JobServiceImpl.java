package com.rev.app.service.impl;

import com.rev.app.dto.JobDto;
import com.rev.app.entity.Employer;
import com.rev.app.entity.Job;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobMapper;
import com.rev.app.entity.Notification;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.IApplicationNoteRepository;
import com.rev.app.repository.IEmployerRepository;
import com.rev.app.repository.IJobRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.INotificationRepository;
import com.rev.app.repository.ISavedJobRepository;
import com.rev.app.service.IJobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements IJobService {

    private final IJobRepository jobRepository;
    private final IEmployerRepository employerRepository;
    private final IJobSeekerRepository jobSeekerRepository;
    private final INotificationRepository notificationRepository;
    private final IApplicationRepository applicationRepository;
    private final IApplicationNoteRepository applicationNoteRepository;
    private final ISavedJobRepository savedJobRepository;
    private final JobMapper jobMapper;

    public JobServiceImpl(IJobRepository jobRepository, IEmployerRepository employerRepository,
            IJobSeekerRepository jobSeekerRepository, INotificationRepository notificationRepository,
            IApplicationRepository applicationRepository,
            IApplicationNoteRepository applicationNoteRepository,
            ISavedJobRepository savedJobRepository,
            JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.employerRepository = employerRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.notificationRepository = notificationRepository;
        this.applicationRepository = applicationRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobMapper = jobMapper;
    }

    @Override
    @Transactional
    public JobDto createJob(Long employerId, JobDto dto) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = new Job();
        job.setCompany(employer.getCompany());
        job.setEmployer(employer);
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setEducationRequired(dto.getEducationRequired());
        job.setLocation(dto.getLocation());
        job.setSalaryRange(dto.getSalaryRange());
        job.setJobType(dto.getJobType());
        job.setDeadline(dto.getDeadline());
        job.setOpenings(dto.getOpenings() != null ? dto.getOpenings() : 1);
        job.setStatus("ACTIVE");
        job.setIsDeleted(false);
        Job saved = jobRepository.save(job);

        // Notify matching seekers (Recommendation)
        String skills = job.getSkillsRequired();
        if (skills != null && !skills.isEmpty()) {
            jobSeekerRepository.findAll().forEach(seeker -> {
                if (seeker.getSkills() != null
                        && seeker.getSkills().toLowerCase().contains(skills.split(",")[0].trim().toLowerCase())) {
                    Notification n = new Notification();
                    n.setUser(seeker.getUser());
                    n.setMessage("New Job Match: " + job.getTitle() + " at " + job.getCompany().getName());
                    n.setType("JOB_RECOMMENDATION");
                    n.setIsRead(false);
                    notificationRepository.save(n);
                }
            });
        }
        return jobMapper.toDto(saved);
    }

    @Override
    @Transactional
    public JobDto updateJob(Long employerId, Long jobId, JobDto dto) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (Boolean.TRUE.equals(job.getIsDeleted())) {
            throw new ResourceNotFoundException("Job not found");
        }

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
        job.setOpenings(dto.getOpenings() != null ? dto.getOpenings() : job.getOpenings());
        if (dto.getStatus() != null) {
            job.setStatus(dto.getStatus());
        }
        return jobMapper.toDto(jobRepository.save(job));
    }

    @Override
    @Transactional(readOnly = true)
    public JobDto getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (Boolean.TRUE.equals(job.getIsDeleted())) {
            throw new ResourceNotFoundException("Job not found");
        }
        JobDto dto = jobMapper.toDto(job);
        populateJobStats(dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobDto> getAllJobs(String title, String location, Integer experience, String company, String salary,
            String jobType, java.time.LocalDate postedDate, org.springframework.data.domain.Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                String keyword = "%" + title.toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), keyword);
                Predicate companyPred = cb.like(cb.lower(root.join("company").get("name")), keyword);
                Predicate skillsPred = cb.like(cb.lower(root.get("skillsRequired")), keyword);
                predicates.add(cb.or(titlePred, companyPred, skillsPred));
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
            if (salary != null && !salary.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("salaryRange")), "%" + salary.toLowerCase() + "%"));
            }
            if (postedDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("postedDate"), postedDate));
            }
            // Only show ACTIVE jobs to seekers
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable).map(job -> {
            JobDto dto = jobMapper.toDto(job);
            populateJobStats(dto);
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobDto> getJobsByEmployer(Long employerId, String status, Pageable pageable) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Page<Job> jobs;
        if (status != null && !status.isEmpty()) {
            jobs = jobRepository.findByCompanyIdAndStatusAndIsDeletedFalse(employer.getCompany().getId(), status,
                    pageable);
        } else {
            jobs = jobRepository.findByCompanyIdAndIsDeletedFalse(employer.getCompany().getId(), pageable);
        }
        return jobs.map(job -> {
            JobDto dto = jobMapper.toDto(job);
            populateJobStats(dto);
            return dto;
        });
    }

    private void populateJobStats(JobDto dto) {
        if (dto == null || dto.getId() == null)
            return;
        Map<String, Long> counts = new HashMap<>();
        String[] statuses = { "Applied", "Under Review", "Shortlisted", "Rejected", "Withdrawn" };
        for (String s : statuses) {
            counts.put(s, applicationRepository.countByJobIdAndStatus(dto.getId(), s));
        }
        dto.setStatusCounts(counts);
        dto.setShortlistedCount(counts.getOrDefault("Shortlisted", 0L));
    }

    @Override
    @Transactional
    public void deleteJob(Long employerId, Long jobId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("You don't own this job.");
        }

        // 1. Manually handle dependent records as requested to ensure safety in Oracle
        System.out.println("DEBUG: Deleting dependent records for Job ID: " + jobId);

        // Delete application notes first (lowest child)
        applicationNoteRepository.deleteByJobId(jobId);

        // Delete all applications for this job
        applicationRepository.deleteByJobId(jobId);

        // Delete all saved jobs entries for this job
        savedJobRepository.deleteByJobId(jobId);

        // 2. Delete notifications that reference this job
        notificationRepository.deleteByReferenceId(jobId);

        // 3. Finally, use delete() which triggers @SQLDelete (soft delete) on the Job
        // entity
        jobRepository.delete(job);
        System.out.println("DEBUG: Job " + jobId + " soft-deleted successfully.");
    }
}
