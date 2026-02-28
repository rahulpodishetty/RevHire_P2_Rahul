package com.rev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rev.app.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
