package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(name = "company_size", length = 50) // 'size' is reserved in Oracle, so naming column 'company_size'
    private String size;

    @Column(length = 2000)
    private String description;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String location;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Job> jobs = new ArrayList<>();
}
