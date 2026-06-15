package com.seal.seal_hackathon_fpt.features.competition.service;

import com.seal.seal_hackathon_fpt.features.competition.dto.CreateCompetitionRequest;
import com.seal.seal_hackathon_fpt.features.competition.dto.PublicCompetitionResponse;
import com.seal.seal_hackathon_fpt.features.competition.dto.UpdateCompetitionRequest;
import com.seal.seal_hackathon_fpt.features.competition.entity.Competition;
import com.seal.seal_hackathon_fpt.features.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    // =========================
    // PUBLIC API - GUEST DASHBOARD
    // =========================

    public List<PublicCompetitionResponse> getPublicCompetitions() {
        return competitionRepository.findAll()
                .stream()
                .filter(this::isPublicVisible)
                .map(this::toPublicResponse)
                .toList();
    }

    public PublicCompetitionResponse getPublicCompetitionById(Long id) {
        Competition competition = getById(id);

        if (!isPublicVisible(competition)) {
            throw new RuntimeException("Competition is not public");
        }

        return toPublicResponse(competition);
    }

    private boolean isPublicVisible(Competition competition) {
        return competition.getStatus() != Competition.Status.Draft
                && competition.getStatus() != Competition.Status.Cancelled;
    }

    private PublicCompetitionResponse toPublicResponse(Competition competition) {
        return PublicCompetitionResponse.builder()
                .id(competition.getId())
                .name(competition.getName())
                .description(competition.getDescription())
                .category(competition.getCategory())
                .location(competition.getLocation())
                .format(competition.getFormat())
                .status(competition.getStatus())
                .startDate(competition.getStartDate())
                .durationDays(competition.getDurationDays())
                .endDate(calculateEndDate(competition))
                .registrationDeadline(competition.getRegistrationDeadline())
                .registrationStatus(getRegistrationStatus(competition))
                .build();
    }

    private LocalDateTime calculateEndDate(Competition competition) {
        if (competition.getStartDate() == null || competition.getDurationDays() == null) {
            return null;
        }

        int daysToAdd = Math.max(competition.getDurationDays() - 1, 0);
        return competition.getStartDate().plusDays(daysToAdd);
    }

    private String getRegistrationStatus(Competition competition) {
        if (competition.getStatus() != Competition.Status.Open) {
            return "Closed";
        }

        if (competition.getRegistrationDeadline() == null) {
            return "Open";
        }

        if (LocalDateTime.now().isAfter(competition.getRegistrationDeadline())) {
            return "Closed";
        }

        return "Open";
    }

    // =========================
    // ADMIN / COORDINATOR CRUD
    // =========================

    public Competition create(CreateCompetitionRequest request) {
        Competition competition = Competition.builder()
                .seasonId(request.getSeasonId())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .location(request.getLocation())
                .format(request.getFormat() != null ? request.getFormat() : Competition.Format.Offline)
                .startDate(request.getStartDate())
                .durationDays(request.getDurationDays())
                .registrationDeadline(request.getRegistrationDeadline())
                .minTeams(request.getMinTeams())
                .minMembers(request.getMinMembers())
                .maxMembers(request.getMaxMembers())
                .scoreScale(request.getScoreScale() != null ? request.getScoreScale() : 100)
                .status(request.getStatus() != null ? request.getStatus() : Competition.Status.Draft)
                .rankingPublished(request.getRankingPublished() != null ? request.getRankingPublished() : false)
                .build();

        return competitionRepository.save(competition);
    }

    public List<Competition> getAll() {
        return competitionRepository.findAll();
    }

    public Competition getById(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
    }

    public Competition update(Long id, UpdateCompetitionRequest request) {
        Competition existing = getById(id);

        if (request.getSeasonId() != null) {
            existing.setSeasonId(request.getSeasonId());
        }

        if (request.getName() != null) {
            existing.setName(request.getName());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory());
        }

        if (request.getLocation() != null) {
            existing.setLocation(request.getLocation());
        }

        if (request.getFormat() != null) {
            existing.setFormat(request.getFormat());
        }

        if (request.getStartDate() != null) {
            existing.setStartDate(request.getStartDate());
        }

        if (request.getDurationDays() != null) {
            existing.setDurationDays(request.getDurationDays());
        }

        if (request.getRegistrationDeadline() != null) {
            existing.setRegistrationDeadline(request.getRegistrationDeadline());
        }

        if (request.getMinTeams() != null) {
            existing.setMinTeams(request.getMinTeams());
        }

        if (request.getMinMembers() != null) {
            existing.setMinMembers(request.getMinMembers());
        }

        if (request.getMaxMembers() != null) {
            existing.setMaxMembers(request.getMaxMembers());
        }

        if (request.getScoreScale() != null) {
            existing.setScoreScale(request.getScoreScale());
        }

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        if (request.getRankingPublished() != null) {
            existing.setRankingPublished(request.getRankingPublished());
        }

        return competitionRepository.save(existing);
    }

    public void delete(Long id) {
        Competition existing = getById(id);
        competitionRepository.delete(existing);
    }
}