package com.rev.app.repository;

import com.rev.app.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IApplicationRepository
                extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
        Page<Application> findByJobSeekerId(Long jobSeekerId, Pageable pageable);

        Page<Application> findByJobSeekerIdAndStatus(Long jobSeekerId, String status, Pageable pageable);

        @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH j.company c JOIN FETCH a.jobSeeker js JOIN FETCH js.user u JOIN FETCH a.resume r WHERE c.id = :companyId")
        Page<Application> findByJobCompanyId(@Param("companyId") Long companyId, Pageable pageable);

        @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH j.company c JOIN FETCH a.jobSeeker js JOIN FETCH js.user u JOIN FETCH a.resume r WHERE c.id = :companyId AND a.status = :status")
        Page<Application> findByJobCompanyIdAndStatus(@Param("companyId") Long companyId,
                        @Param("status") String status,
                        Pageable pageable);

        @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH a.jobSeeker js JOIN FETCH js.user u JOIN FETCH a.resume r WHERE j.id = :jobId")
        Page<Application> findByJobId(@Param("jobId") Long jobId, Pageable pageable);

        boolean existsByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

        long countByJobSeekerId(Long jobSeekerId);

        long countByJobCompanyId(Long companyId);

        long countByJobCompanyIdAndStatus(Long companyId, String status);

        long countByJobCompanyIdAndStatusIn(Long companyId, java.util.List<String> statuses);

        long countByJobIdAndStatus(Long jobId, String status);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @org.springframework.data.jpa.repository.Query("DELETE FROM Application a WHERE a.job.id = :jobId")
        void deleteByJobId(@Param("jobId") Long jobId);
}
