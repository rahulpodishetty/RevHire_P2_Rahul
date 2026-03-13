package com.rev.app.repository;

import com.rev.app.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByJobSeekerId(Long jobSeekerId);

    java.util.Optional<Resume> findByJobSeekerIdAndIsActive(Long jobSeekerId, Boolean isActive);
}
