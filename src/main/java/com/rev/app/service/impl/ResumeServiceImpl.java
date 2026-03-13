package com.rev.app.service.impl;

import com.rev.app.dto.ResumeDto;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.Resume;
import com.rev.app.exception.CustomAccessDeniedException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.ResumeMapper;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.IResumeRepository;
import com.rev.app.service.IResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements IResumeService {

    private final IResumeRepository IResumeRepository;
    private final IJobSeekerRepository IJobSeekerRepository;
    private final ResumeMapper resumeMapper;
    private final String UPLOAD_DIR = "uploads/resumes";

    public ResumeServiceImpl(IResumeRepository IResumeRepository, IJobSeekerRepository IJobSeekerRepository,
            ResumeMapper resumeMapper) {
        this.IResumeRepository = IResumeRepository;
        this.IJobSeekerRepository = IJobSeekerRepository;
        this.resumeMapper = resumeMapper;
    }

    @Override
    @Transactional
    public ResumeDto saveResume(Long jobSeekerId, ResumeDto dto) {
        JobSeeker seeker = IJobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        Resume resume;
        if (dto.getId() != null) {
            resume = IResumeRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
            if (!resume.getJobSeeker().getId().equals(jobSeekerId)) {
                throw new CustomAccessDeniedException("Not authorized to update this resume");
            }
        } else {
            resume = new Resume();
            resume.setJobSeeker(seeker);
            // If it's the first resume, make it active
            if (IResumeRepository.findByJobSeekerId(jobSeekerId).isEmpty()) {
                resume.setIsActive(true);
            } else {
                resume.setIsActive(false);
            }
        }

        resume.setObjective(dto.getObjective());
        resume.setEducation(dto.getEducation());
        resume.setExperience(dto.getExperience());
        resume.setSkills(dto.getSkills());
        resume.setProjects(dto.getProjects());
        resume.setCertifications(dto.getCertifications());

        return resumeMapper.toDto(IResumeRepository.save(resume));
    }

    @Override
    @Transactional
    public ResumeDto uploadResume(Long jobSeekerId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate File Type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Only PDF and DOCX files are allowed");
        }

        // Validate File Size (2MB max)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        JobSeeker seeker = IJobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        try {
            // Ensure Upload Directory Exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate Unique Filename
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : ".pdf";
            String savedFileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(savedFileName);

            // Save Physically
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save to DB
            Resume resume = Resume.builder()
                    .jobSeeker(seeker)
                    .fileName(originalFileName)
                    .filePath(filePath.toString())
                    .isActive(IResumeRepository.findByJobSeekerId(jobSeekerId).isEmpty()) // Active if first
                    .build();

            return resumeMapper.toDto(IResumeRepository.save(resume));

        } catch (IOException e) {
            throw new RuntimeException("Failed to save resume file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void setActiveResume(Long resumeId, Long jobSeekerId) {
        List<Resume> resumes = IResumeRepository.findByJobSeekerId(jobSeekerId);
        boolean found = false;
        for (Resume resume : resumes) {
            if (resume.getId().equals(resumeId)) {
                resume.setIsActive(true);
                found = true;
            } else {
                resume.setIsActive(false);
            }
        }
        if (!found) {
            throw new ResourceNotFoundException("Resume not found for this seeker");
        }
        IResumeRepository.saveAll(resumes);
    }

    @Override
    public ResumeDto getActiveResume(Long jobSeekerId) {
        return IResumeRepository.findByJobSeekerIdAndIsActive(jobSeekerId, true)
                .map(resumeMapper::toDto)
                .orElseGet(() -> {
                    List<Resume> resumes = IResumeRepository.findByJobSeekerId(jobSeekerId);
                    if (!resumes.isEmpty()) {
                        return resumeMapper.toDto(resumes.get(0));
                    }
                    return null;
                });
    }

    @Override
    public ResumeDto getResumeById(Long id) {
        return resumeMapper.toDto(IResumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found")));
    }

    @Override
    public List<ResumeDto> getResumesByJobSeeker(Long jobSeekerId) {
        return IResumeRepository.findByJobSeekerId(jobSeekerId)
                .stream().map(resumeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteResume(Long id, Long jobSeekerId) {
        Resume resume = IResumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (!resume.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new CustomAccessDeniedException("Not authorized to delete this resume");
        }
        IResumeRepository.delete(resume);

        // If we deleted the active one, pick another one to be active if exists
        if (Boolean.TRUE.equals(resume.getIsActive())) {
            List<Resume> resumes = IResumeRepository.findByJobSeekerId(jobSeekerId);
            if (!resumes.isEmpty()) {
                resumes.get(0).setIsActive(true);
                IResumeRepository.save(resumes.get(0));
            }
        }
    }
}
