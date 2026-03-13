package com.rev.app.service;

import com.rev.app.dto.EmployerDto;

import java.util.Map;

public interface IEmployerService {
    EmployerDto getProfileByUserId(Long userId);

    EmployerDto updateProfile(Long userId, EmployerDto dto);

    Map<String, Object> getDashboardStats(Long userId);

    void uploadCompanyLogo(Long employerId, org.springframework.web.multipart.MultipartFile file);
}
