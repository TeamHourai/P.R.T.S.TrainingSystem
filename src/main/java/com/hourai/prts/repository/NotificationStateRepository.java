package com.hourai.prts.repository;

import com.hourai.prts.entity.NotificationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationStateRepository extends JpaRepository<NotificationState, Long> {
    List<NotificationState> findByUserId(Long userId);
    Optional<NotificationState> findByUserIdAndNotificationId(Long userId, Long notificationId);
    long countByUserIdAndIsReadFalse(Long userId);
    Page<NotificationState> findByUserIdAndIsHiddenFalse(Long userId, Pageable pageable);
}
