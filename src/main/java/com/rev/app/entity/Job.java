package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE jobs SET is_deleted = 1 WHERE job_id = ?")
@Where(clause = "is_deleted = 0")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    private Company company;

    /** The employer who posted this job. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    @ToString.Exclude
    private Employer employer;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "skills_required", length = 1000)
    private String skillsRequired;

    @Column(name = "experience_required")
    private Integer experienceRequired;

    @Column(name = "education_required", length = 500)
    private String educationRequired;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "salary_range", length = 100)
    private String salaryRange;

    @Column(name = "job_type", length = 50)
    private String jobType;

    @Column(nullable = false)
    private LocalDate deadline;

    @Builder.Default
    @Column(name = "openings")
    private Integer openings = 1;

    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 30)
    private String status = "ACTIVE";

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "posted_date", updatable = false)
    private LocalDate postedDate;

    @Builder.Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Application> applications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SavedJob> savedJobs = new ArrayList<>();
}
