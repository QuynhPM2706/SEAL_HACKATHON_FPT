package com.seal.seal_hackathon_fpt.notification.controller;

import com.seal.seal_hackathon_fpt.notification.entity.Notification;
import com.seal.seal_hackathon_fpt.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification
    ) {

        return ResponseEntity.ok(
                notificationService.createNotification(notification)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>>
    getNotifications(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(userId)
        );
    }
}