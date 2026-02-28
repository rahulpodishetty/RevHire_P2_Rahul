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
    private final EmployerMapper employerMapper;

    public EmployerServiceImpl(IEmployerRepository IEmployerRepository, ICompanyRepository ICompanyRepository,
            IJobRepository IJobRepository, IApplicationRepository IApplicationRepository,
            EmployerMapper employerMapper) {
        this.IEmployerRepository = IEmployerRepository;
        this.ICompanyRepository = ICompanyRepository;
        this.IJobRepository = IJobRepository;
        this.IApplicationRepository = IApplicationRepository;
        this.employerMapper = employerMapper;
    }

    @Override
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
    public java.util.Map<String, Object> getDashboardStats(Long userId) {
        Employer employer = IEmployerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Long companyId = employer.getCompany().getId();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalJobs", IJobRepository.countByCompanyId(companyId));
        stats.put("totalApplicants", IApplicationRepository.countByJobCompanyId(companyId));
        stats.put("activeJobs", IJobRepository.countByCompanyId(companyId)); // For simplicity
        return stats;
    }
}
