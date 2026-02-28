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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements IResumeService {

    private final IResumeRepository IResumeRepository;
    private final IJobSeekerRepository IJobSeekerRepository;
    private final ResumeMapper resumeMapper;

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

        Resume resume = new Resume();
        if (dto.getId() != null) {
            resume = IResumeRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
            if (!resume.getJobSeeker().getId().equals(jobSeekerId)) {
                throw new CustomAccessDeniedException("Not authorized to update this resume");
            }
        } else {
            resume.setJobSeeker(seeker);
        }

        resume.setObjective(dto.getObjective());
        resume.setEducation(dto.getEducation());
        resume.setExperience(dto.getExperience());
        resume.setSkills(dto.getSkills());
        resume.setProjects(dto.getProjects());
        resume.setFilePath(dto.getFilePath());

        return resumeMapper.toDto(IResumeRepository.save(resume));
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
    }
}

