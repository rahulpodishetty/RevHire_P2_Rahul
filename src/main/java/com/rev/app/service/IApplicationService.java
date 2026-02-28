package com.rev.app.service;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.dto.ApplicationNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IApplicationService {
    ApplicationDto applyToJob(Long jobSeekerId, Long jobId, Long resumeId, String coverLetter);

    ApplicationDto withdrawApplication(Long jobSeekerId, Long applicationId, String reason);

    Page<ApplicationDto> getApplicationsByJobSeeker(Long jobSeekerId, Pageable pageable);

    Page<ApplicationDto> getApplicationsByJob(Long employerId, Long jobId, Pageable pageable);

    ApplicationDto updateApplicationStatus(Long employerId, Long applicationId, String status);

    ApplicationNoteDto addNoteToApplication(Long employerId, Long applicationId, String noteText);
}

