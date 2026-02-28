package com.rev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rev.app.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
