package com.rev.app.service;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.dto.ApplicationNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IApplicationService {
    ApplicationDto applyToJob(Long jobSeekerId, Long jobId, Long resumeId, String coverLetter);

    boolean hasApplied(Long jobSeekerId, Long jobId);

    ApplicationDto withdrawApplication(Long jobSeekerId, Long applicationId, String reason);

    Page<ApplicationDto> getApplicationsByJobSeeker(Long jobSeekerId, String status, Pageable pageable);

    Page<ApplicationDto> getApplicationsByJob(Long employerId, Long jobId, Pageable pageable);

    Page<ApplicationDto> getApplicationsByEmployer(Long employerId, String status, Pageable pageable);

    ApplicationDto updateApplicationStatus(Long employerId, Long applicationId, String status);

    void updateApplicationStatusesBulk(Long employerId, java.util.List<Long> applicationIds, String status);

    ApplicationNoteDto addNoteToApplication(Long employerId, Long applicationId, String noteText);

    Page<ApplicationDto> getFilteredApplications(Long employerId, String status, String skills, Integer minExp,
            String education, Pageable pageable);
}
