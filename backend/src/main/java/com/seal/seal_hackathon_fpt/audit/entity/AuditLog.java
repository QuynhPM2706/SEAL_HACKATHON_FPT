package com.seal.seal_hackathon_fpt.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(nullable = false)
    private Boolean flagged;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}