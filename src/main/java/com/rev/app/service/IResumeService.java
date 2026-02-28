package com.rev.app.service;

import com.rev.app.dto.ResumeDto;
import java.util.List;

public interface IResumeService {
    ResumeDto saveResume(Long jobSeekerId, ResumeDto dto);

    ResumeDto getResumeById(Long id);

    List<ResumeDto> getResumesByJobSeeker(Long jobSeekerId);

    void deleteResume(Long id, Long jobSeekerId);
}

