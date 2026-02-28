package com.rev.app.mapper;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {
    public ApplicationDto toDto(Application app) {
        if (app == null)
            return null;
        return ApplicationDto.builder()
                .id(app.getId())
                .jobId(app.getJob() != null ? app.getJob().getId() : null)
                .jobSeekerId(app.getJobSeeker() != null ? app.getJobSeeker().getId() : null)
                .resumeId(app.getResume() != null ? app.getResume().getId() : null)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : null)
                .companyName(
                        app.getJob() != null && app.getJob().getCompany() != null ? app.getJob().getCompany().getName()
                                : null)
                .jobSeekerName(app.getJobSeeker() != null ? app.getJobSeeker().getName() : null)
                .coverLetter(app.getCoverLetter())
                .status(app.getStatus())
                .appliedDate(app.getAppliedDate())
                .withdrawReason(app.getWithdrawReason())
                .build();
    }
}
