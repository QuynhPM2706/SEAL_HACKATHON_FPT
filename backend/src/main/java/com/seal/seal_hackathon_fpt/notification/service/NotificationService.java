package com.seal.seal_hackathon_fpt.notification.service;

import com.seal.seal_hackathon_fpt.notification.entity.Notification;
import com.seal.seal_hackathon_fpt.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(
            Notification notification
    ) {

        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications(
            Long userId
    ) {

        return notificationRepository.findByUserId(userId);
    }
}