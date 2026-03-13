package com.rev.app.service.impl;

import com.rev.app.dto.JobSeekerDto;
import com.rev.app.entity.JobSeeker;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.JobSeekerMapper;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.ISavedJobRepository;
import com.rev.app.repository.IUserRepository;
import com.rev.app.repository.INotificationRepository;
import com.rev.app.service.IJobSeekerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobSeekerServiceImpl implements IJobSeekerService {

    private final IJobSeekerRepository IJobSeekerRepository;
    private final IUserRepository IUserRepository;
    private final IApplicationRepository IApplicationRepository;
    private final ISavedJobRepository ISavedJobRepository;
    private final INotificationRepository INotificationRepository;
    private final JobSeekerMapper jobSeekerMapper;

    public JobSeekerServiceImpl(IJobSeekerRepository IJobSeekerRepository,
            IUserRepository IUserRepository,
            IApplicationRepository IApplicationRepository,
            ISavedJobRepository ISavedJobRepository,
            INotificationRepository INotificationRepository,
            JobSeekerMapper jobSeekerMapper) {
        this.IJobSeekerRepository = IJobSeekerRepository;
        this.IUserRepository = IUserRepository;
        this.IApplicationRepository = IApplicationRepository;
        this.ISavedJobRepository = ISavedJobRepository;
        this.INotificationRepository = INotificationRepository;
        this.jobSeekerMapper = jobSeekerMapper;
    }

    @Override
    @Transactional
    public JobSeekerDto getProfileByUserId(Long userId) {
        return IJobSeekerRepository.findByUserId(userId)
                .map(jobSeekerMapper::toDto)
                .orElseGet(() -> {
                    System.out.println("DEBUG: JobSeeker profile missing for user " + userId + ". Creating stub...");
                    com.rev.app.entity.User user = IUserRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                    JobSeeker newProfile = new JobSeeker();
                    newProfile.setUser(user);
                    newProfile.setName(user.getEmail().split("@")[0]);
                    newProfile.setProfileCompletion(0);
                    JobSeeker saved = IJobSeekerRepository.save(newProfile);
                    return jobSeekerMapper.toDto(saved);
                });
    }

    @Override
    @Transactional
    public JobSeekerDto updateProfile(Long userId, JobSeekerDto dto) {
        JobSeeker jobSeeker = IJobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found for User ID: " + userId));

        jobSeeker.setName(dto.getName() != null ? dto.getName() : jobSeeker.getName());
        jobSeeker.setPhone(dto.getPhone() != null ? dto.getPhone() : jobSeeker.getPhone());
        jobSeeker.setExperienceYears(
                dto.getExperienceYears() != null ? dto.getExperienceYears() : jobSeeker.getExperienceYears());
        jobSeeker.setLocation(dto.getLocation() != null ? dto.getLocation() : jobSeeker.getLocation());
        jobSeeker.setCurrentEmploymentStatus(
                dto.getCurrentEmploymentStatus() != null ? dto.getCurrentEmploymentStatus()
                        : jobSeeker.getCurrentEmploymentStatus());
        jobSeeker.setEducation(dto.getEducation() != null ? dto.getEducation() : jobSeeker.getEducation());
        jobSeeker.setWorkExperience(
                dto.getWorkExperience() != null ? dto.getWorkExperience() : jobSeeker.getWorkExperience());
        jobSeeker.setSkills(dto.getSkills() != null ? dto.getSkills() : jobSeeker.getSkills());
        jobSeeker.setCertifications(
                dto.getCertifications() != null ? dto.getCertifications() : jobSeeker.getCertifications());
        jobSeeker.setProjects(
                dto.getProjects() != null ? dto.getProjects() : jobSeeker.getProjects());

        // Recalculate completion
        jobSeeker.setProfileCompletion(calculateProfileStrength(jobSeeker.getId()));

        JobSeeker updated = IJobSeekerRepository.save(jobSeeker);
        return jobSeekerMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(Long userId) {
        JobSeeker profile = IJobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", IApplicationRepository.countByJobSeekerId(profile.getId()));
        stats.put("savedJobsCount", ISavedJobRepository.countByJobSeekerId(profile.getId()));
        stats.put("notificationsCount", INotificationRepository.countByUserIdAndIsReadFalse(userId));
        stats.put("profileCompletion", calculateProfileStrength(profile.getId()));
        return stats;
    }

    @Override
    public int calculateProfileStrength(Long seekerId) {
        JobSeeker seeker = IJobSeekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        int strength = 0;

        // 1. Basic Info (Name, Phone, ExpYears, Location, Status) - 20%
        if (seeker.getName() != null && !seeker.getName().isEmpty())
            strength += 4;
        if (seeker.getPhone() != null && !seeker.getPhone().isEmpty())
            strength += 4;
        if (seeker.getExperienceYears() != null && seeker.getExperienceYears() >= 0)
            strength += 4;
        if (seeker.getLocation() != null && !seeker.getLocation().isEmpty())
            strength += 4;
        if (seeker.getCurrentEmploymentStatus() != null && !seeker.getCurrentEmploymentStatus().isEmpty())
            strength += 4;

        // 2. Profile Details (Education, Work Exp, Skills, Certs, Projects) - 50%
        if (seeker.getEducation() != null && !seeker.getEducation().isEmpty())
            strength += 10;
        if (seeker.getWorkExperience() != null && !seeker.getWorkExperience().isEmpty())
            strength += 10;
        if (seeker.getSkills() != null && !seeker.getSkills().isEmpty())
            strength += 10;
        if (seeker.getCertifications() != null && !seeker.getCertifications().isEmpty())
            strength += 10;
        if (seeker.getProjects() != null && !seeker.getProjects().isEmpty())
            strength += 10;

        // 3. Active Resume Check - 30%
        List<com.rev.app.entity.Resume> resumes = seeker.getResumes();
        if (resumes != null && !resumes.isEmpty()) {
            boolean hasActive = resumes.stream().anyMatch(r -> r.getIsActive());
            if (hasActive)
                strength += 30;
        }

        return Math.min(strength, 100);
    }

    @Override
    @Transactional
    public void uploadProfileImage(Long seekerId, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        JobSeeker seeker = IJobSeekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeeker not found"));

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = java.util.UUID.randomUUID().toString() + extension;
            String uploadDir = "uploads/seeker-profile";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            java.nio.file.Path targetLocation = uploadPath.resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), targetLocation,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            seeker.setProfileImagePath("/uploads/seeker-profile/" + fileName);
            IJobSeekerRepository.save(seeker);
            System.out.println(
                    "DEBUG: Profile image saved for seeker " + seekerId + " at " + seeker.getProfileImagePath());
        } catch (java.io.IOException ex) {
            System.err.println("ERROR: Failed to store profile image: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Could not store image. Please try again!", ex);
        }
    }
}
