package com.rev.app.mapper;

import com.rev.app.dto.EmployerDto;
import com.rev.app.entity.Employer;
import org.springframework.stereotype.Component;

@Component
public class EmployerMapper {
    private final CompanyMapper companyMapper;

    public EmployerMapper(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    public EmployerDto toDto(Employer employer) {
        if (employer == null)
            return null;
        return EmployerDto.builder()
                .id(employer.getId())
                .userId(employer.getUser() != null ? employer.getUser().getId() : null)
                .designation(employer.getDesignation())
                .company(companyMapper.toDto(employer.getCompany()))
                .build();
    }
}
