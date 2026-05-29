package com.seal.seal_hackathon_fpt.notification.repository;

import com.seal.seal_hackathon_fpt.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);
}