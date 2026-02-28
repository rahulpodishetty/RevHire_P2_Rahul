package com.rev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rev.app.entity.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
