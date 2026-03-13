package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDto {
    private Long id;
    private Long userId;
    private CompanyDto company;
    private String designation;

    public String getLogoPath() {
        return company != null ? company.getLogoPath() : null;
    }
}
