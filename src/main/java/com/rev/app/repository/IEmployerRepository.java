package com.rev.app.repository;

import com.rev.app.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IEmployerRepository extends JpaRepository<Employer, Long> {
    @Query("SELECT e FROM Employer e JOIN FETCH e.user JOIN FETCH e.company WHERE e.user.id = :userId")
    Optional<Employer> findByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Employer e JOIN FETCH e.user JOIN FETCH e.company WHERE e.id = :id")
    Optional<Employer> findById(@Param("id") Long id);
}
