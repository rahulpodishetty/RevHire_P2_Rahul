package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @ToString.Exclude
    private JobSeeker jobSeeker;

    @Column(length = 2000)
    private String objective;

    @Column(length = 2000)
    private String education;

    @Column(length = 2000)
    private String experience;

    @Column(length = 1000)
    private String skills;

    @Column(length = 2000)
    private String projects;

    @Column(name = "file_path", length = 500)
    private String filePath;
}
