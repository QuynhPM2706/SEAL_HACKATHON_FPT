package com.seal.seal_hackathon_fpt.features.competition.dto;

import com.seal.seal_hackathon_fpt.features.competition.entity.Competition;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateCompetitionRequest {
    private Long seasonId;
    private String name;
    private String description;
    private String category;
    private String location;

    private Competition.Format format;
    private LocalDateTime startDate;
    private Integer durationDays;
    private LocalDateTime registrationDeadline;

    private Integer minTeams;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer scoreScale;

    private Competition.Status status;
    private Boolean rankingPublished;
}