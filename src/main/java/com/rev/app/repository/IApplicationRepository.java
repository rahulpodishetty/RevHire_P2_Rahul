package com.rev.app.repository;

import com.rev.app.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IApplicationRepository extends JpaRepository<Application, Long> {
    Page<Application> findByJobSeekerId(Long jobSeekerId, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    boolean existsByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
}

