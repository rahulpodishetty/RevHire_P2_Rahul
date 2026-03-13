package com.rev.app.service;

import com.rev.app.dto.ResumeDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IResumeService {
    ResumeDto saveResume(Long jobSeekerId, ResumeDto dto);

    ResumeDto getResumeById(Long id);

    List<ResumeDto> getResumesByJobSeeker(Long jobSeekerId);

    void deleteResume(Long id, Long jobSeekerId);

    ResumeDto uploadResume(Long jobSeekerId, MultipartFile file);

    void setActiveResume(Long resumeId, Long jobSeekerId);

    ResumeDto getActiveResume(Long jobSeekerId);
}
