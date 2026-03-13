package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @ToString.Exclude
    private JobSeeker jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @ToString.Exclude
    private Resume resume;

    @Column(name = "cover_letter", length = 4000)
    private String coverLetter;

    @Column(nullable = false, length = 50)
    private String status; // e.g., APPLIED, SHORTLISTED, REJECTED, WITHDRAWN

    @CreationTimestamp
    @Column(name = "applied_date", updatable = false)
    private LocalDateTime appliedDate;

    @Column(name = "withdraw_reason", length = 1000)
    private String withdrawReason;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ApplicationNote> applicationNotes = new ArrayList<>();
}
