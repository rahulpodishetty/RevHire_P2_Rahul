package com.rev.app.repository;

import com.rev.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    List<Notification> findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    org.springframework.data.domain.Page<Notification> findByUserId(Long userId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Notification n WHERE n.referenceId = :refId")
    void deleteByReferenceId(@org.springframework.data.repository.query.Param("refId") Long refId);
}
