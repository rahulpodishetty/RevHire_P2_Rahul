package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_seekers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeeker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_seeker_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "current_employment_status", length = 50)
    private String currentEmploymentStatus;

    @Column(name = "education", length = 2000)
    private String education;

    @Column(name = "work_experience", length = 4000)
    private String workExperience;

    @Column(name = "skills", length = 2000)
    private String skills;

    @Column(name = "certifications", length = 2000)
    private String certifications;

    @Column(name = "projects", length = 4000)
    private String projects;

    @Column(name = "profile_completion")
    private Integer profileCompletion;

    @Column(name = "profile_image_path", length = 255)
    private String profileImagePath;

    @Builder.Default
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Resume> resumes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Application> applications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SavedJob> savedJobs = new ArrayList<>();
}
