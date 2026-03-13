package com.rev.app.repository;

import com.rev.app.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IJobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyIdAndIsDeletedFalse(Long companyId, Pageable pageable);

    Page<Job> findByCompanyIdAndStatusAndIsDeletedFalse(Long companyId, String status, Pageable pageable);

    long countByCompanyIdAndIsDeletedFalse(Long companyId);

    long countByCompanyIdAndStatusAndIsDeletedFalse(Long companyId, String status);

    /**
     * Count applications for a specific job without loading the lazy collection.
     * Used by JobMapper to safely populate applicantCount.
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId")
    long countApplicationsByJobId(@Param("jobId") Long jobId);
}
