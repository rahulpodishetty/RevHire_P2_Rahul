package com.rev.app.repository;

import com.rev.app.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByJobSeekerId(Long jobSeekerId);

    boolean existsByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    void deleteByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    long countByJobSeekerId(Long jobSeekerId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM SavedJob s WHERE s.job.id = :jobId")
    void deleteByJobId(@org.springframework.data.repository.query.Param("jobId") Long jobId);
}
