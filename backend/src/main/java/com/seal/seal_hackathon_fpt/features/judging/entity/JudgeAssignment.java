package com.seal.seal_hackathon_fpt.features.judging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "judge_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "judge_id",
                                "round_id",
                                "team_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "judge_id", nullable = false)
    private Long judgeId;

    @Column(name = "competition_id", nullable = false)
    private Long competitionId;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}