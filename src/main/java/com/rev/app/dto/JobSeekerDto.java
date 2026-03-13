package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerDto {
    private Long id;
    private Long userId; // For reference
    private String name;
    private String phone;
    private String email;
    private Integer experienceYears;
    private String location;
    private String currentEmploymentStatus;
    private String education;
    private String workExperience;
    private String skills;
    private String certifications;
    private String projects;
    private Integer profileCompletion;
    private String profileImagePath;
}
