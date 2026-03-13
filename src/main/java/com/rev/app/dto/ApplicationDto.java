package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private Long jobId;
    private Long jobSeekerId;
    private Long resumeId;

    // Details needed for frontend listings without extra calls
    private String jobTitle;
    private String companyName;
    private String jobSeekerName;

    private JobDto job;
    private JobSeekerDto jobSeeker;
    private ResumeDto resume;

    private String coverLetter;
    private String status;
    private LocalDateTime appliedDate;
    private String withdrawReason;
    private String notes;
    private java.util.List<ApplicationNoteDto> applicationNotes;
}
