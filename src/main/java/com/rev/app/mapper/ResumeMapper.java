package com.rev.app.mapper;

import com.rev.app.dto.ResumeDto;
import com.rev.app.entity.Resume;
import org.springframework.stereotype.Component;

@Component
public class ResumeMapper {
    public ResumeDto toDto(Resume resume) {
        if (resume == null)
            return null;
        return ResumeDto.builder()
                .id(resume.getId())
                .jobSeekerId(resume.getJobSeeker() != null ? resume.getJobSeeker().getId() : null)
                .objective(resume.getObjective())
                .education(resume.getEducation())
                .experience(resume.getExperience())
                .skills(resume.getSkills())
                .projects(resume.getProjects())
                .filePath(resume.getFilePath())
                .build();
    }
}
