package com.seal.seal_hackathon_fpt.audit.service;

import com.seal.seal_hackathon_fpt.audit.entity.AuditLog;
import com.seal.seal_hackathon_fpt.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            Long userId,
            String userName,
            String action,
            String entityType,
            String entityId
    ) {

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .userName(userName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .flagged(false)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}