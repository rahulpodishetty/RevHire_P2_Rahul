package com.rev.app.mapper;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.entity.Application;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ApplicationMapper {
    private final JobMapper jobMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final ResumeMapper resumeMapper;
    private final ApplicationNoteMapper applicationNoteMapper;

    public ApplicationMapper(JobMapper jobMapper, JobSeekerMapper jobSeekerMapper, ResumeMapper resumeMapper,
            ApplicationNoteMapper applicationNoteMapper) {
        this.jobMapper = jobMapper;
        this.jobSeekerMapper = jobSeekerMapper;
        this.resumeMapper = resumeMapper;
        this.applicationNoteMapper = applicationNoteMapper;
    }

    public ApplicationDto toDto(Application app) {
        if (app == null)
            return null;
        return ApplicationDto.builder()
                .id(app.getId())
                .jobId(app.getJob() != null ? app.getJob().getId() : null)
                .jobSeekerId(app.getJobSeeker() != null ? app.getJobSeeker().getId() : null)
                .resumeId(app.getResume() != null ? app.getResume().getId() : null)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : "Job No Longer Available")
                .companyName(
                        app.getJob() != null && app.getJob().getCompany() != null ? app.getJob().getCompany().getName()
                                : "N/A")
                .jobSeekerName(app.getJobSeeker() != null ? app.getJobSeeker().getName() : null)
                .job(jobMapper.toDto(app.getJob()))
                .jobSeeker(jobSeekerMapper.toDto(app.getJobSeeker()))
                .resume(resumeMapper.toDto(app.getResume()))
                .coverLetter(app.getCoverLetter())
                .status(app.getStatus())
                .appliedDate(app.getAppliedDate())
                .withdrawReason(app.getWithdrawReason())
                .notes(app.getNotes())
                .applicationNotes(app.getApplicationNotes() != null ? app.getApplicationNotes().stream()
                        .map(applicationNoteMapper::toDto).collect(Collectors.toList()) : null)
                .build();
    }
}
