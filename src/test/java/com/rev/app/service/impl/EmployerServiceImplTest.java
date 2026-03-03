package com.rev.app.service.impl;

import com.rev.app.dto.CompanyDto;
import com.rev.app.dto.EmployerDto;
import com.rev.app.entity.Company;
import com.rev.app.entity.Employer;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EmployerMapper;
import com.rev.app.repository.IApplicationRepository;
import com.rev.app.repository.ICompanyRepository;
import com.rev.app.repository.IEmployerRepository;
import com.rev.app.repository.IJobRepository;
import com.rev.app.repository.INotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerServiceImplTest {

    @Mock
    private IEmployerRepository employerRepository;
    @Mock
    private ICompanyRepository companyRepository;
    @Mock
    private IJobRepository jobRepository;
    @Mock
    private IApplicationRepository applicationRepository;
    @Mock
    private INotificationRepository notificationRepository;
    @Mock
    private EmployerMapper employerMapper;

    @InjectMocks
    private EmployerServiceImpl employerService;

    private Employer testEmployer;
    private Company testCompany;
    private User testUser;
    private EmployerDto testEmployerDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCompany = new Company();
        testCompany.setId(10L);
        testCompany.setName("TechCorp");
        testCompany.setIndustry("IT");

        testEmployer = new Employer();
        testEmployer.setId(100L);
        testEmployer.setUser(testUser);
        testEmployer.setCompany(testCompany);
        testEmployer.setDesignation("HR Manager");

        testEmployerDto = new EmployerDto();
        testEmployerDto.setId(100L);
        testEmployerDto.setDesignation("HR Manager");
    }

    @Test
    void getProfileByUserId_Success() {
        when(employerRepository.findByUserId(1L)).thenReturn(Optional.of(testEmployer));
        when(employerMapper.toDto(testEmployer)).thenReturn(testEmployerDto);

        EmployerDto result = employerService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals("HR Manager", result.getDesignation());
        verify(employerRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getProfileByUserId_NotFound() {
        when(employerRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employerService.getProfileByUserId(999L));
    }

    @Test
    void updateProfile_Success() {
        EmployerDto updateDto = new EmployerDto();
        updateDto.setDesignation("Senior HR");
        CompanyDto companyDto = new CompanyDto();
        companyDto.setName("NewTechCorp");
        updateDto.setCompany(companyDto);

        when(employerRepository.findByUserId(1L)).thenReturn(Optional.of(testEmployer));
        when(employerRepository.save(any(Employer.class))).thenReturn(testEmployer);
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        when(employerMapper.toDto(any(Employer.class))).thenReturn(testEmployerDto);

        EmployerDto result = employerService.updateProfile(1L, updateDto);

        assertNotNull(result);
        verify(employerRepository, times(1)).save(any(Employer.class));
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    void getDashboardStats_Success() {
        when(employerRepository.findByUserId(1L)).thenReturn(Optional.of(testEmployer));
        when(jobRepository.countByCompanyIdAndIsDeletedFalse(10L)).thenReturn(5L);
        when(jobRepository.countByCompanyIdAndStatusAndIsDeletedFalse(10L, "ACTIVE")).thenReturn(3L);
        when(applicationRepository.countByJobCompanyId(10L)).thenReturn(20L);
        when(applicationRepository.countByJobCompanyIdAndStatus(10L, "Applied")).thenReturn(10L);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(2L);

        Map<String, Object> stats = employerService.getDashboardStats(1L);

        assertEquals(5L, stats.get("totalJobs"));
        assertEquals(3L, stats.get("activeJobs"));
        assertEquals(20L, stats.get("totalApplicants"));
        assertEquals(10L, stats.get("pendingReviews"));
        assertEquals(2L, stats.get("notificationsCount"));
    }
}
