package com.rev.app.service.impl;

import com.rev.app.dto.CompanyDto;
import com.rev.app.dto.EmployerDto;
import com.rev.app.entity.Company;
import com.rev.app.entity.Employer;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EmployerMapper;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.ICompanyRepository;
import com.rev.app.repository.IEmployerRepository;
import com.rev.app.repository.IJobRepository;
import com.rev.app.service.IEmployerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerServiceImpl implements IEmployerService {

    private final IEmployerRepository IEmployerRepository;
    private final ICompanyRepository ICompanyRepository;
    private final IJobRepository IJobRepository;
    private final IApplicationRepository IApplicationRepository;
    private final com.rev.app.repository.INotificationRepository INotificationRepository;
    private final EmployerMapper employerMapper;

    public EmployerServiceImpl(IEmployerRepository IEmployerRepository, ICompanyRepository ICompanyRepository,
            IJobRepository IJobRepository, IApplicationRepository IApplicationRepository,
            com.rev.app.repository.INotificationRepository INotificationRepository,
            EmployerMapper employerMapper) {
        this.IEmployerRepository = IEmployerRepository;
        this.ICompanyRepository = ICompanyRepository;
        this.IJobRepository = IJobRepository;
        this.IApplicationRepository = IApplicationRepository;
        this.INotificationRepository = INotificationRepository;
        this.employerMapper = employerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerDto getProfileByUserId(Long userId) {
        Employer employer = IEmployerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for User ID: " + userId));
        return employerMapper.toDto(employer);
    }

    @Override
    @Transactional
    public EmployerDto updateProfile(Long userId, EmployerDto dto) {
        Employer employer = IEmployerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for User ID: " + userId));

        employer.setDesignation(dto.getDesignation() != null ? dto.getDesignation() : employer.getDesignation());

        if (dto.getCompany() != null) {
            Company company = employer.getCompany();
            CompanyDto companyDto = dto.getCompany();
            company.setName(companyDto.getName() != null ? companyDto.getName() : company.getName());
            company.setIndustry(companyDto.getIndustry() != null ? companyDto.getIndustry() : company.getIndustry());
            company.setSize(companyDto.getSize() != null ? companyDto.getSize() : company.getSize());
            company.setDescription(
                    companyDto.getDescription() != null ? companyDto.getDescription() : company.getDescription());
            company.setWebsite(companyDto.getWebsite() != null ? companyDto.getWebsite() : company.getWebsite());
            company.setLocation(companyDto.getLocation() != null ? companyDto.getLocation() : company.getLocation());
            ICompanyRepository.save(company);
        }

        Employer updated = IEmployerRepository.save(employer);
        return employerMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDashboardStats(Long userId) {
        Employer employer = IEmployerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Long companyId = employer.getCompany().getId();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalJobs", IJobRepository.countByCompanyIdAndIsDeletedFalse(companyId));
        stats.put("activeJobs", IJobRepository.countByCompanyIdAndStatusAndIsDeletedFalse(companyId, "ACTIVE"));
        stats.put("totalApplicants", IApplicationRepository.countByJobCompanyId(companyId));
        stats.put("pendingReviews", IApplicationRepository.countByJobCompanyIdAndStatus(companyId, "Applied"));
        stats.put("notificationsCount", INotificationRepository.countByUserIdAndIsReadFalse(userId));
        return stats;
    }

    @Override
    @Transactional
    public void uploadCompanyLogo(Long employerId, org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        Employer employer = IEmployerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Company company = employer.getCompany();

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path targetLocation = java.nio.file.Paths.get("uploads/company-logos").resolve(fileName);

            // Create directories if they do not exist
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("uploads/company-logos"));

            java.nio.file.Files.copy(file.getInputStream(), targetLocation,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            company.setLogoPath("/uploads/company-logos/" + fileName);
            ICompanyRepository.save(company);
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Could not store image. Please try again!", ex);
        }
    }
}
