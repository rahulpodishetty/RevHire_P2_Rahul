package com.rev.app.mapper;

import com.rev.app.dto.CompanyDto;
import com.rev.app.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
    public CompanyDto toDto(Company company) {
        if (company == null)
            return null;
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .size(company.getSize())
                .description(company.getDescription())
                .website(company.getWebsite())
                .location(company.getLocation())
                .build();
    }
}
