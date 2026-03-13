package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String name;
    private String industry;
    private String size; // 'company_size' in entity mapped to 'size' here
    private String description;
    private String website;
    private String location;
    private String logoPath;
}
