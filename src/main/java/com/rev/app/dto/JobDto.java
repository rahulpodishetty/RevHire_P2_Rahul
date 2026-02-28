package com.rev.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private CompanyDto company;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String skillsRequired;
    private Integer experienceRequired;
    private String educationRequired;
    private String location;
    private String salaryRange;
    private String jobType;
    private LocalDate deadline;
}
