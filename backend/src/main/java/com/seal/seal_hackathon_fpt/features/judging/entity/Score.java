package com.seal.seal_hackathon_fpt.features.judging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "judge_id",
                                "team_id",
                                "round_id",
                                "criterion_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "judge_id", nullable = false)
    private Long judgeId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "criterion_id", nullable = false)
    private Long criterionId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(nullable = false)
    private String status;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}