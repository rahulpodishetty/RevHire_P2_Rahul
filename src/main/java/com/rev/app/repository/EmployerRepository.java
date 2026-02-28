package com.rev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rev.app.entity.Employer;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {
}
