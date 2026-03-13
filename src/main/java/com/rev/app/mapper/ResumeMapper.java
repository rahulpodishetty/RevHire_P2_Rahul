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
                .certifications(resume.getCertifications())
                .filePath(resume.getFilePath())
                .fileName(resume.getFileName())
                .isActive(resume.getIsActive() != null ? resume.getIsActive() : false)
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    public Resume toEntity(ResumeDto dto) {
        if (dto == null)
            return null;
        return Resume.builder()
                .id(dto.getId())
                .objective(dto.getObjective())
                .education(dto.getEducation())
                .experience(dto.getExperience())
                .skills(dto.getSkills())
                .projects(dto.getProjects())
                .certifications(dto.getCertifications())
                .filePath(dto.getFilePath())
                .fileName(dto.getFileName())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : false)
                .build();
    }
}
