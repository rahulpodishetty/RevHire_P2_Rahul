package com.rev.app.repository;

import com.rev.app.entity.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {
    List<ApplicationNote> findByApplicationId(Long applicationId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM ApplicationNote an WHERE an.application.job.id = :jobId")
    void deleteByJobId(@org.springframework.data.repository.query.Param("jobId") Long jobId);
}
