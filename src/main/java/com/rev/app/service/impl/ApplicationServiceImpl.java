package com.rev.app.service.impl;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.dto.ApplicationNoteDto;
import com.rev.app.entity.*;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.ApplicationMapper;
import com.rev.app.mapper.ApplicationNoteMapper;
import com.rev.app.repository.*;
import com.rev.app.service.IApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationServiceImpl implements IApplicationService {

    private final IApplicationRepository IApplicationRepository;
    private final IJobRepository IJobRepository;
    private final IJobSeekerRepository IJobSeekerRepository;
    private final IEmployerRepository IEmployerRepository;
    private final IResumeRepository IResumeRepository;
    private final IApplicationNoteRepository IApplicationNoteRepository;
    private final INotificationRepository INotificationRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationNoteMapper applicationNoteMapper;

    public ApplicationServiceImpl(IApplicationRepository IApplicationRepository, IJobRepository IJobRepository,
            IJobSeekerRepository IJobSeekerRepository, IEmployerRepository IEmployerRepository,
            IResumeRepository IResumeRepository, IApplicationNoteRepository IApplicationNoteRepository,
            INotificationRepository INotificationRepository, ApplicationMapper applicationMapper,
            ApplicationNoteMapper applicationNoteMapper) {
        this.IApplicationRepository = IApplicationRepository;
        this.IJobRepository = IJobRepository;
        this.IJobSeekerRepository = IJobSeekerRepository;
        this.IEmployerRepository = IEmployerRepository;
        this.IResumeRepository = IResumeRepository;
        this.IApplicationNoteRepository = IApplicationNoteRepository;
        this.INotificationRepository = INotificationRepository;
        this.applicationMapper = applicationMapper;
        this.applicationNoteMapper = applicationNoteMapper;
    }

    @Override
    @Transactional
    public ApplicationDto applyToJob(Long jobSeekerId, Long jobId, Long resumeId, String coverLetter) {
        if (IApplicationRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId)) {
            throw new IllegalArgumentException("Already applied to this job.");
        }

        JobSeeker jobSeeker = IJobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));
        Job job = IJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!"ACTIVE".equals(job.getStatus())) {
            throw new IllegalArgumentException(
                    "This job is no longer accepting applications (Current Status: " + job.getStatus() + ")");
        }

        if (resumeId == null) {
            throw new IllegalArgumentException("Resume is required for application.");
        }

        Resume resume = IResumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new CustomAccessDeniedException("Resume does not belong to you.");
        }

        Application application = new Application();
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setResume(resume);
        application.setCoverLetter(coverLetter);
        application.setStatus("Applied");

        Application saved = IApplicationRepository.save(application);

        // Notify employer
        if (job.getEmployer() != null) {
            Notification notification = new Notification();
            notification.setUser(job.getEmployer().getUser());
            notification.setTitle("New Job Application");
            notification.setMessage(jobSeeker.getName() + " applied for " + job.getTitle());
            notification.setType("APPLICATION");
            notification.setReferenceId(saved.getId());
            notification.setIsRead(false);
            INotificationRepository.save(notification);
        }

        return applicationMapper.toDto(saved);
    }

    @Override
    public boolean hasApplied(Long jobSeekerId, Long jobId) {
        return IApplicationRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId);
    }

    @Override
    @Transactional
    public ApplicationDto withdrawApplication(Long jobSeekerId, Long applicationId, String reason) {
        Application application = IApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new CustomAccessDeniedException("Unauthorized application withdrawal.");
        }

        application.setStatus("Withdrawn");
        application.setWithdrawReason(reason);
        Application saved = IApplicationRepository.save(application);

        // Notify employer
        if (application.getJob().getEmployer() != null) {
            Notification notification = new Notification();
            notification.setUser(application.getJob().getEmployer().getUser());
            notification.setTitle("Application Withdrawn");
            notification.setMessage(application.getJobSeeker().getName() + " withdrawn application for "
                    + application.getJob().getTitle());
            notification.setType("WITHDRAWAL");
            notification.setReferenceId(saved.getId());
            notification.setIsRead(false);
            INotificationRepository.save(notification);
        }

        return applicationMapper.toDto(saved);
    }

    @Override
    public Page<ApplicationDto> getApplicationsByJobSeeker(Long jobSeekerId, String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return IApplicationRepository.findByJobSeekerIdAndStatus(jobSeekerId, status, pageable)
                    .map(applicationMapper::toDto);
        }
        return IApplicationRepository.findByJobSeekerId(jobSeekerId, pageable).map(applicationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsByJob(Long employerId, Long jobId, Pageable pageable) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Job job = IJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("You don't own this job description");
        }

        return IApplicationRepository.findByJobId(jobId, pageable).map(applicationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsByEmployer(Long employerId, String status, Pageable pageable) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Long companyId = employer.getCompany().getId();

        Page<Application> applications;
        if (status != null && !status.isEmpty()) {
            applications = IApplicationRepository.findByJobCompanyIdAndStatus(companyId, status, pageable);
        } else {
            applications = IApplicationRepository.findByJobCompanyId(companyId, pageable);
        }

        return applications.map(applicationMapper::toDto);
    }

    @Override
    @Transactional
    public ApplicationDto updateApplicationStatus(Long employerId, Long applicationId, String status) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Application application = IApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("Unauthorized to update this application");
        }

        application.setStatus(status);
        Application saved = IApplicationRepository.save(application);

        // Notify seeker
        Notification notification = new Notification();
        notification.setUser(application.getJobSeeker().getUser());
        notification.setTitle("Application Status Update");
        notification
                .setMessage("Your application status for " + application.getJob().getTitle() + " changed to " + status);
        notification.setType("STATUS_UPDATE");
        notification.setReferenceId(saved.getId());
        notification.setIsRead(false);
        INotificationRepository.save(notification);

        return applicationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void updateApplicationStatusesBulk(Long employerId, List<Long> applicationIds, String status) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        List<Application> applications = IApplicationRepository.findAllById(applicationIds);
        for (Application app : applications) {
            if (app.getJob().getCompany().getId().equals(employer.getCompany().getId())) {
                app.setStatus(status);
                // Notification
                Notification notification = new Notification();
                notification.setUser(app.getJobSeeker().getUser());
                notification.setTitle("Application Status Update");
                notification
                        .setMessage("Your application status for " + app.getJob().getTitle() + " changed to " + status);
                notification.setType("STATUS_UPDATE");
                notification.setReferenceId(app.getId());
                notification.setIsRead(false);
                INotificationRepository.save(notification);
            }
        }
        IApplicationRepository.saveAll(applications);
    }

    @Override
    @Transactional
    public ApplicationNoteDto addNoteToApplication(Long employerId, Long applicationId, String noteText) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Application application = IApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompany().getId().equals(employer.getCompany().getId())) {
            throw new CustomAccessDeniedException("Unauthorized to note on this application");
        }

        ApplicationNote note = new ApplicationNote();
        note.setApplication(application);
        note.setEmployer(employer);
        note.setNoteText(noteText);

        // Sync to Application's notes string field
        String currentNotes = application.getNotes();
        if (currentNotes == null || currentNotes.isEmpty()) {
            application.setNotes(noteText);
        } else {
            application.setNotes(currentNotes + " | " + noteText);
        }
        IApplicationRepository.save(application);

        // Notify seeker
        Notification notification = new Notification();
        notification.setUser(application.getJobSeeker().getUser());
        notification.setTitle("New Note on Application");
        notification.setMessage("The employer added a note to your application for " + application.getJob().getTitle());
        notification.setType("NOTE");
        notification.setReferenceId(application.getId());
        notification.setIsRead(false);
        INotificationRepository.save(notification);

        return applicationNoteMapper.toDto(IApplicationNoteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getFilteredApplications(Long employerId, String status, String skills, Integer minExp,
            String education, Pageable pageable) {
        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Long companyId = employer.getCompany().getId();

        return IApplicationRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.join("job").join("company").get("id"), companyId));

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (skills != null && !skills.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.join("resume").get("skills")), "%" + skills.toLowerCase() + "%"));
            }
            if (minExp != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.join("jobSeeker").get("experienceYears"), minExp));
            }
            if (education != null && !education.isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.join("resume").get("education")), "%" + education.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(applicationMapper::toDto);
    }
}
