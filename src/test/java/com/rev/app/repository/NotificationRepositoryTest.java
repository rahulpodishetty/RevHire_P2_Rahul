package com.rev.app.repository;

import com.rev.app.entity.Notification;
import com.rev.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private INotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole("ROLE_SEEKER");
        entityManager.persist(testUser);

        Notification n1 = Notification.builder()
                .user(testUser)
                .title("T1")
                .message("M1")
                .isRead(false)
                .build();
        entityManager.persist(n1);

        Notification n2 = Notification.builder()
                .user(testUser)
                .title("T2")
                .message("M2")
                .isRead(true)
                .build();
        entityManager.persist(n2);

        entityManager.flush();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_Success() {
        List<Notification> results = notificationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertEquals(2, results.size());
    }

    @Test
    void countByUserIdAndIsReadFalse_Success() {
        long count = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());
        assertEquals(1, count);
    }
}
