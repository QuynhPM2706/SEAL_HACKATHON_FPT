package com.seal.seal_hackathon_fpt.features.competition.dto;

import com.seal.seal_hackathon_fpt.features.competition.entity.Competition;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicCompetitionResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String location;

    private Competition.Format format;
    private Competition.Status status;

    private LocalDateTime startDate;
    private Integer durationDays;

    // Không lưu DB, backend tự tính từ startDate + durationDays
    private LocalDateTime endDate;

    private LocalDateTime registrationDeadline;

    // Chỉ có 2 giá trị: Open / Closed
    private String registrationStatus;
}