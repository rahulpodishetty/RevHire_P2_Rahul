package com.rev.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private String skillsRequired;

    @Min(value = 0, message = "Experience required cannot be negative")
    private Integer experienceRequired;

    private String educationRequired;

    @NotBlank(message = "Job location is required")
    private String location;

    private String salaryRange;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotNull(message = "Application deadline is required")
    private LocalDate deadline;

    @Min(value = 1, message = "Number of openings must be at least 1")
    private Integer openings;

    private String status;

    private LocalDate postedDate;

    /** Populated from a COUNT query — not mapped from the lazy collection. */
    private Long applicantCount;

    /** Statistics breakdown. */
    private java.util.Map<String, Long> statusCounts;
    private Long shortlistedCount;
}
