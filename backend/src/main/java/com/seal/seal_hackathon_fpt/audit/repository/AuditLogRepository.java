package com.seal.seal_hackathon_fpt.audit.repository;

import com.seal.seal_hackathon_fpt.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {
}