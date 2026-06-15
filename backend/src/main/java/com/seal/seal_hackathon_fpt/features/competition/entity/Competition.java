package com.seal.seal_hackathon_fpt.features.competition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_id")
    private Long seasonId;

    private String name;

    private String description;

    private String category;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Format format;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "min_teams")
    private Integer minTeams;

    @Column(name = "min_members")
    private Integer minMembers;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "score_scale")
    private Integer scoreScale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "ranking_published")
    private Boolean rankingPublished;

    public enum Format {
        Offline,
        Online,
        Hybrid
    }

    public enum Status {
        Draft,
        Open,
        Active,
        Scoring,
        Closed,
        Cancelled
    }
}