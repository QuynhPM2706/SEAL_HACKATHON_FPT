package com.seal.seal_hackathon_fpt.features.judging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "score_overrides",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "team_id",
                                "round_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "override_score", nullable = false)
    private BigDecimal overrideScore;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}