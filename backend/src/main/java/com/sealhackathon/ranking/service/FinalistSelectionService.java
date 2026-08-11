package com.sealhackathon.ranking.service;

import com.sealhackathon.common.exception.BusinessException;
import com.sealhackathon.common.exception.ResourceNotFoundException;
import com.sealhackathon.event.domain.Round;
import com.sealhackathon.event.domain.enums.EventStatus;
import com.sealhackathon.event.domain.enums.RoundType;
import com.sealhackathon.event.repository.HackathonEventRepository;
import com.sealhackathon.event.repository.RoundRepository;
import com.sealhackathon.event.repository.TrackRepository;
import com.sealhackathon.event.service.EventStatusResolver;
import com.sealhackathon.event.service.FormatRuleEngine;
import com.sealhackathon.judging.service.JudgingPublicService;
import com.sealhackathon.ranking.domain.FinalistContestedSlot;
import com.sealhackathon.ranking.domain.FinalistContestedSlotTeam;
import com.sealhackathon.ranking.domain.FinalistSelection;
import com.sealhackathon.ranking.domain.Ranking;
import com.sealhackathon.ranking.domain.enums.ContestedSlotType;
import com.sealhackathon.ranking.domain.enums.FinalistSelectionMethod;
import com.sealhackathon.ranking.dto.request.SelectFinalistsRequest;
import com.sealhackathon.ranking.dto.response.ContestedSlotResponse;
import com.sealhackathon.ranking.dto.response.ContestedTeamResponse;
import com.sealhackathon.ranking.dto.response.FinalistResponse;
import com.sealhackathon.ranking.dto.response.FinalistSelectResultResponse;
import com.sealhackathon.ranking.dto.response.FinalistSelectionSummaryResponse;
import com.sealhackathon.ranking.repository.FinalistContestedSlotRepository;
import com.sealhackathon.ranking.repository.FinalistSelectionRepository;
import com.sealhackathon.ranking.repository.RankingRepository;
import com.sealhackathon.submission.service.FinalSubmissionCarryOverService;
import com.sealhackathon.submission.service.SubmissionPublicService;
import com.sealhackathon.team.dto.snapshot.TeamSnapshot;
import com.sealhackathon.team.service.TeamPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinalistSelectionService {

    private final FinalistSelectionRepository finalistRepository;
    private final FinalistContestedSlotRepository contestedSlotRepository;
    private final RankingRepository rankingRepository;
    private final RoundRepository roundRepository;
    private final HackathonEventRepository eventRepository;
    private final TrackRepository trackRepository;
    private final TeamPublicService teamPublicService;
    private final SubmissionPublicService submissionPublicService;
    private final EventStatusResolver eventStatusResolver;
    private final RankingTieBreakComparator tieBreakComparator;
    private final FormatRuleEngine formatRuleEngine;
    private final AdvancementCutoffCalculator cutoffCalculator;
    private final FinalSubmissionCarryOverService finalSubmissionCarryOverService;
    private final AdvancementService advancementService;
    private final JudgingPublicService judgingPublicService;

    @Transactional
    public FinalistSelectResultResponse selectFinalists(UUID eventId) {
        return selectFinalists(eventId, null, false);
    }

    @Transactional
    public FinalistSelectResultResponse selectFinalists(UUID eventId, SelectFinalistsRequest request) {
        return selectFinalists(eventId, request, false);
    }

    @Transactional(readOnly = true)
    public FinalistSelectResultResponse previewFinalists(UUID eventId, SelectFinalistsRequest request) {
        return selectFinalists(eventId, request, true);
    }

    private FinalistSelectResultResponse selectFinalists(
            UUID eventId, SelectFinalistsRequest request, boolean preview) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        EventStatus resolved = eventStatusResolver.resolveStatus(event);
        if (resolved != EventStatus.SCORING && resolved != EventStatus.ACTIVE
                && resolved != EventStatus.COMPLETED) {
            throw new BusinessException(
                    "Finalist selection is only allowed when event is ACTIVE, SCORING, or COMPLETED",
                    HttpStatus.BAD_REQUEST);
        }

        Round preliminary = roundRepository.findByHackathonEventIdOrderByRoundNumberAsc(eventId).stream()
                .filter(r -> r.getRoundType() == RoundType.PRELIMINARY)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No preliminary round found for this event", HttpStatus.BAD_REQUEST));

        int latestVersion = rankingRepository.findMaxVersionByRoundId(preliminary.getId());
        if (latestVersion == 0) {
            throw new BusinessException("Preliminary rankings not yet calculated", HttpStatus.BAD_REQUEST);
        }

        Round finalRound = roundRepository.findByHackathonEventIdOrderByRoundNumberAsc(eventId).stream()
                .filter(r -> r.getRoundType() == RoundType.FINAL)
                .findFirst()
                .orElse(null);

        if (!preview) {
            validateFinalNotStarted(finalRound);
        }

        List<Ranking> rankings = rankingRepository
                .findByRoundIdAndVersionOrderByRankAsc(preliminary.getId(), latestVersion);

        SelectFinalistsRequest.SelectionMode mode = request != null && request.getMode() != null
                ? request.getMode()
                : SelectFinalistsRequest.SelectionMode.AUTO;
        Integer explicitTopN = request != null ? request.getTopN() : null;

        SelectionState state = new SelectionState();
        if (mode == SelectFinalistsRequest.SelectionMode.MANUAL) {
            selectManualFinalists(rankings, request != null ? request.getTeamIds() : null, state);
            state.bucketScope = "MANUAL";
            state.bucketLabel = "manual";
        } else if (formatRuleEngine.isSealFormat(event)) {
            selectSealFinalists(preliminary.getId(), rankings, state, explicitTopN);
        } else {
            selectGenericFinalists(preliminary, rankings, state, explicitTopN);
        }

        List<FinalistResponse> finalistResponses;
        List<ContestedSlotResponse> contestedResponses;

        if (preview) {
            LocalDateTime now = LocalDateTime.now();
            finalistResponses = toPreviewResponses(eventId, state, now);
            contestedResponses = toPreviewContested(preliminary.getId(), state);
        } else {
            finalistRepository.deleteByEventId(eventId);
            contestedSlotRepository.deleteByEventId(eventId);
            finalistRepository.flush();
            contestedSlotRepository.flush();

            LocalDateTime now = LocalDateTime.now();
            List<FinalistSelection> saved = persistSelections(eventId, state, now);
            List<FinalistContestedSlot> savedSlots = persistContestedSlots(eventId, preliminary.getId(), state);

            finalistResponses = saved.stream().map(this::toResponse).toList();
            contestedResponses = savedSlots.stream()
                    .map(s -> toContestedResponse(s, preliminary.getId()))
                    .toList();

            // Carry previous-round submissions into FINAL for each finalist
            if (finalRound != null) {
                List<UUID> teamIds = finalistResponses.stream().map(FinalistResponse::getTeamId).toList();
                finalSubmissionCarryOverService.carryOverForTeams(finalRound.getId(), teamIds);
            }

            if (explicitTopN != null && mode == SelectFinalistsRequest.SelectionMode.AUTO) {
                preliminary.setAdvancementCutoff(explicitTopN);
                roundRepository.save(preliminary);
            }

            Set<UUID> advancedIds = new HashSet<>(state.selectedTeamIds);
            advancementService.syncAdvancements(preliminary.getId(), advancedIds);
        }

        int targetCount = explicitTopN != null
                ? Math.max(finalistResponses.size(), 1)
                : (cutoffCalculator.isAutoEnabled()
                        ? Math.max(finalistResponses.size(), 1)
                        : (formatRuleEngine.isSealFormat(event)
                                ? formatRuleEngine.getSealFinalistCount()
                                : preliminary.getAdvancementCutoff()));

        boolean penaltyRequired = contestedResponses.stream()
                .anyMatch(ContestedSlotResponse::isNeedsPenaltyEvaluation);

        return FinalistSelectResultResponse.builder()
                .finalists(finalistResponses)
                .contestedSlots(contestedResponses)
                .summary(FinalistSelectionSummaryResponse.builder()
                        .selectedCount(finalistResponses.size())
                        .targetCount(targetCount)
                        .penaltyEvaluationRequired(penaltyRequired)
                        .bucketScope(state.bucketScope)
                        .bucketLabel(state.bucketLabel)
                        .topN(explicitTopN)
                        .build())
                .build();
    }

    /**
     * Re-confirming wipes and rebuilds the finalist set, so it must stop once the Final panel has
     * started scoring — otherwise those scores would be orphaned.
     */
    private void validateFinalNotStarted(Round finalRound) {
        if (finalRound == null) {
            return;
        }
        boolean scoringStarted = judgingPublicService.existsLockedScoreByRound(finalRound.getId())
                || !judgingPublicService.countCompletedScoresByRound(finalRound.getId()).isEmpty();
        if (scoringStarted) {
            throw new BusinessException(
                    "Final round scoring has already started — advancement can no longer be changed",
                    HttpStatus.CONFLICT);
        }
    }

    private void selectManualFinalists(List<Ranking> rankings, List<UUID> teamIds, SelectionState state) {
        if (teamIds == null || teamIds.isEmpty()) {
            throw new BusinessException("Manual selection requires at least one teamId", HttpStatus.BAD_REQUEST);
        }
        Map<UUID, Ranking> byTeam = new LinkedHashMap<>();
        for (Ranking r : rankings) {
            byTeam.put(r.getTeamId(), r);
        }
        List<Ranking> ordered = new ArrayList<>();
        for (UUID teamId : teamIds) {
            Ranking r = byTeam.get(teamId);
            if (r == null) {
                throw new BusinessException(
                        "Team is not in preliminary rankings: " + teamId, HttpStatus.BAD_REQUEST);
            }
            ordered.add(r);
        }
        ordered.sort(java.util.Comparator.comparing(Ranking::getRank));
        for (Ranking r : ordered) {
            state.addSelection(r.getTeamId(), FinalistSelectionMethod.MANUAL,
                    "Manually advanced", false);
        }
    }

    private void selectGenericFinalists(
            Round preliminary, List<Ranking> rankings, SelectionState state, Integer explicitTopN) {
        if (selectByGroup(rankings, preliminary.getId(), state, explicitTopN)) {
            return;
        }
        if (selectByTrack(rankings, preliminary.getId(), state, explicitTopN)) {
            return;
        }
        int cutoff = resolveCutoff(rankings.size(), explicitTopN,
                preliminary.getAdvancementCutoff() != null ? preliminary.getAdvancementCutoff() : 3);
        RankingTieBreakComparator.SelectionCutResult cut = tieBreakComparator.cutTopN(
                rankings, cutoff, preliminary.getId());
        for (Ranking r : cut.selected()) {
            state.addSelection(r.getTeamId(), FinalistSelectionMethod.TOP_PER_TRACK,
                    "Advancement cutoff " + cutoff, false);
        }
        if (!cut.contested().isEmpty()) {
            state.addContested(null, ContestedSlotType.PER_TRACK_CUTOFF, 1, cut.contested());
        }
        state.bucketScope = "GLOBAL";
        state.bucketLabel = "overall";
    }

    private void selectSealFinalists(UUID roundId, List<Ranking> rankings,
                                      SelectionState state, Integer explicitTopN) {
        if (selectByGroup(rankings, roundId, state, explicitTopN)) {
            return;
        }
        if (selectByTrack(rankings, roundId, state, explicitTopN)) {
            // Overflow fill only for legacy auto (no explicit Top N)
            if (explicitTopN == null && !cutoffCalculator.isAutoEnabled()) {
                int target = formatRuleEngine.getSealFinalistCount();
                fillOverflow(rankings, roundId, state, target);
            }
            return;
        }
        int cutoff = resolveCutoff(rankings.size(), explicitTopN, formatRuleEngine.getSealTopPerTrack());
        RankingTieBreakComparator.SelectionCutResult cut = tieBreakComparator.cutTopN(
                rankings, cutoff, roundId);
        for (Ranking r : cut.selected()) {
            state.addSelection(r.getTeamId(), FinalistSelectionMethod.TOP_PER_TRACK,
                    "Top overall (cutoff " + cutoff + ")", false);
        }
        if (!cut.contested().isEmpty()) {
            state.addContested(null, ContestedSlotType.PER_TRACK_CUTOFF, 1, cut.contested());
        }
        state.bucketScope = "GLOBAL";
        state.bucketLabel = "overall";
    }

    private void fillOverflow(List<Ranking> rankings, UUID roundId, SelectionState state, int target) {
        if (state.selectedTeamIds.size() >= target) {
            return;
        }
        int needed = target - state.selectedTeamIds.size();
        List<Ranking> remaining = rankings.stream()
                .filter(r -> !state.selectedSet.contains(r.getTeamId()))
                .toList();
        RankingTieBreakComparator.SelectionCutResult overflow = tieBreakComparator.cutTopN(
                remaining, needed, roundId);
        for (Ranking r : overflow.selected()) {
            state.addSelection(r.getTeamId(), FinalistSelectionMethod.OVERFLOW_FILL,
                    "Overflow fill to reach " + target + " finalists", false);
        }
        if (!overflow.contested().isEmpty()) {
            state.addContested(null, ContestedSlotType.OVERFLOW_FILL, 99, overflow.contested());
        }
    }

    /**
     * @return true if at least one competition group was used for selection
     */
    private boolean selectByGroup(
            List<Ranking> rankings, UUID roundId, SelectionState state, Integer explicitTopN) {
        Map<UUID, List<Ranking>> byGroup = new LinkedHashMap<>();
        for (Ranking r : rankings) {
            UUID groupId = teamPublicService.getTeam(r.getTeamId())
                    .map(TeamSnapshot::getGroupId)
                    .orElse(null);
            if (groupId != null) {
                byGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(r);
            }
        }
        if (byGroup.isEmpty()) {
            return false;
        }

        int slotIndex = 1;
        for (Map.Entry<UUID, List<Ranking>> entry : byGroup.entrySet()) {
            int cutoff = resolveCutoff(entry.getValue().size(), explicitTopN, null);
            RankingTieBreakComparator.SelectionCutResult cut = tieBreakComparator.cutTopN(
                    entry.getValue(), cutoff, roundId);
            for (Ranking r : cut.selected()) {
                state.addSelection(r.getTeamId(), FinalistSelectionMethod.TOP_PER_GROUP,
                        "Top in competition group (cutoff " + cutoff + ")", false);
            }
            if (!cut.contested().isEmpty()) {
                state.addContested(null, ContestedSlotType.PER_GROUP_CUTOFF,
                        slotIndex++, cut.contested());
            }
        }
        state.bucketScope = "PER_GROUP";
        state.bucketLabel = "per group";
        return true;
    }

    private boolean selectByTrack(
            List<Ranking> rankings, UUID roundId, SelectionState state, Integer explicitTopN) {
        Map<UUID, List<Ranking>> byTrack = groupByTrack(rankings);
        if (byTrack.isEmpty()) {
            return false;
        }

        int slotIndex = 1;
        for (Map.Entry<UUID, List<Ranking>> entry : byTrack.entrySet()) {
            UUID trackId = entry.getKey();
            int cutoff = resolveCutoff(entry.getValue().size(), explicitTopN,
                    formatRuleEngine.getSealTopPerTrack());
            RankingTieBreakComparator.SelectionCutResult cut = tieBreakComparator.cutTopN(
                    entry.getValue(), cutoff, roundId);

            for (Ranking r : cut.selected()) {
                state.addSelection(r.getTeamId(), FinalistSelectionMethod.TOP_PER_TRACK,
                        "Top in track (cutoff " + cutoff + ")", false);
            }
            if (!cut.contested().isEmpty()) {
                state.addContested(trackId, ContestedSlotType.PER_TRACK_CUTOFF, slotIndex++, cut.contested());
            }
        }
        state.bucketScope = "PER_TRACK";
        state.bucketLabel = "per track";
        return true;
    }

    private int resolveCutoff(int bucketSize, Integer explicitTopN, Integer fallbackFixed) {
        if (bucketSize <= 0) {
            return 0;
        }
        if (explicitTopN != null) {
            return Math.min(Math.max(explicitTopN, 1), bucketSize);
        }
        if (cutoffCalculator.isAutoEnabled()) {
            return cutoffCalculator.compute(bucketSize);
        }
        int fixed = fallbackFixed != null ? fallbackFixed : 1;
        return Math.min(Math.max(fixed, 1), bucketSize);
    }

    private List<FinalistResponse> toPreviewResponses(UUID eventId, SelectionState state, LocalDateTime now) {
        List<FinalistResponse> list = new ArrayList<>();
        int rank = 1;
        for (SelectionEntry entry : state.selections) {
            TeamSnapshot team = teamPublicService.getTeam(entry.teamId()).orElse(null);
            UUID trackId = team != null ? team.getTrackId() : null;
            String trackName = trackId != null
                    ? trackRepository.findById(trackId).map(t -> t.getName()).orElse(null)
                    : null;
            list.add(FinalistResponse.builder()
                    .id(UUID.randomUUID())
                    .eventId(eventId)
                    .teamId(entry.teamId())
                    .teamName(team != null ? team.getName() : null)
                    .trackId(trackId)
                    .trackName(trackName)
                    .preliminaryRank(rank++)
                    .selectedReason(entry.reason())
                    .selectedAt(now)
                    .selectionMethod(entry.method())
                    .needsPenaltyEvaluation(entry.needsPenalty())
                    .build());
        }
        return list;
    }

    private List<ContestedSlotResponse> toPreviewContested(UUID roundId, SelectionState state) {
        List<ContestedSlotResponse> list = new ArrayList<>();
        for (ContestedEntry entry : state.contested) {
            String trackName = entry.trackId() != null
                    ? trackRepository.findById(entry.trackId()).map(t -> t.getName()).orElse(null)
                    : null;
            List<ContestedTeamResponse> teams = entry.rankings().stream()
                    .map(r -> ContestedTeamResponse.builder()
                            .teamId(r.getTeamId())
                            .teamName(teamPublicService.getTeam(r.getTeamId())
                                    .map(TeamSnapshot::getName).orElse(null))
                            .finalScore(r.getFinalScore())
                            .submittedAt(getSubmittedAt(roundId, r.getTeamId()))
                            .build())
                    .toList();
            list.add(ContestedSlotResponse.builder()
                    .id(UUID.randomUUID())
                    .trackId(entry.trackId())
                    .trackName(trackName)
                    .slotType(entry.slotType())
                    .slotIndex(entry.slotIndex())
                    .needsPenaltyEvaluation(true)
                    .teams(teams)
                    .build());
        }
        return list;
    }

    private Map<UUID, List<Ranking>> groupByTrack(List<Ranking> rankings) {
        Map<UUID, List<Ranking>> byTrack = new LinkedHashMap<>();
        for (Ranking r : rankings) {
            UUID trackId = teamPublicService.getTeam(r.getTeamId())
                    .map(TeamSnapshot::getTrackId)
                    .orElse(null);
            if (trackId == null) {
                continue;
            }
            byTrack.computeIfAbsent(trackId, k -> new ArrayList<>()).add(r);
        }
        return byTrack;
    }

    private List<FinalistSelection> persistSelections(UUID eventId, SelectionState state, LocalDateTime now) {
        List<FinalistSelection> saved = new ArrayList<>();
        int rank = 1;
        for (SelectionEntry entry : state.selections) {
            TeamSnapshot team = teamPublicService.getTeam(entry.teamId()).orElse(null);
            saved.add(finalistRepository.save(FinalistSelection.builder()
                    .eventId(eventId)
                    .teamId(entry.teamId())
                    .trackId(team != null ? team.getTrackId() : null)
                    .preliminaryRank(rank++)
                    .selectedReason(entry.reason())
                    .selectedAt(now)
                    .selectionMethod(entry.method())
                    .needsPenaltyEvaluation(entry.needsPenalty())
                    .build()));
        }
        return saved;
    }

    private List<FinalistContestedSlot> persistContestedSlots(UUID eventId, UUID roundId, SelectionState state) {
        List<FinalistContestedSlot> saved = new ArrayList<>();
        for (ContestedEntry entry : state.contested) {
            FinalistContestedSlot slot = FinalistContestedSlot.builder()
                    .eventId(eventId)
                    .trackId(entry.trackId())
                    .slotType(entry.slotType())
                    .slotIndex(entry.slotIndex())
                    .needsPenaltyEvaluation(true)
                    .resolved(false)
                    .build();

            for (Ranking r : entry.rankings()) {
                slot.getTeams().add(FinalistContestedSlotTeam.builder()
                        .contestedSlot(slot)
                        .teamId(r.getTeamId())
                        .finalScore(r.getFinalScore())
                        .submittedAt(getSubmittedAt(roundId, r.getTeamId()))
                        .build());
            }
            saved.add(contestedSlotRepository.save(slot));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FinalistResponse> getFinalists(UUID eventId) {
        return finalistRepository.findByEventIdOrderByPreliminaryRankAsc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContestedSlotResponse> getContestedSlots(UUID eventId) {
        Round preliminary = roundRepository.findByHackathonEventIdOrderByRoundNumberAsc(eventId).stream()
                .filter(r -> r.getRoundType() == RoundType.PRELIMINARY)
                .findFirst()
                .orElse(null);
        UUID roundId = preliminary != null ? preliminary.getId() : null;

        return contestedSlotRepository.findByEventIdAndResolvedFalseOrderBySlotIndexAsc(eventId).stream()
                .map(s -> toContestedResponse(s, roundId))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isFinalist(UUID eventId, UUID teamId) {
        return finalistRepository.existsByEventIdAndTeamId(eventId, teamId);
    }

    private FinalistResponse toResponse(FinalistSelection f) {
        String teamName = teamPublicService.getTeam(f.getTeamId())
                .map(TeamSnapshot::getName)
                .orElse(null);
        String trackName = f.getTrackId() != null
                ? trackRepository.findById(f.getTrackId()).map(t -> t.getName()).orElse(null)
                : null;
        return FinalistResponse.builder()
                .id(f.getId())
                .eventId(f.getEventId())
                .teamId(f.getTeamId())
                .teamName(teamName)
                .trackId(f.getTrackId())
                .trackName(trackName)
                .preliminaryRank(f.getPreliminaryRank())
                .selectedReason(f.getSelectedReason())
                .selectedAt(f.getSelectedAt())
                .selectionMethod(f.getSelectionMethod())
                .needsPenaltyEvaluation(f.isNeedsPenaltyEvaluation())
                .build();
    }

    private ContestedSlotResponse toContestedResponse(FinalistContestedSlot slot, UUID roundId) {
        String trackName = slot.getTrackId() != null
                ? trackRepository.findById(slot.getTrackId()).map(t -> t.getName()).orElse(null)
                : null;
        List<ContestedTeamResponse> teams = slot.getTeams().stream()
                .map(t -> ContestedTeamResponse.builder()
                        .teamId(t.getTeamId())
                        .teamName(teamPublicService.getTeam(t.getTeamId())
                                .map(TeamSnapshot::getName).orElse(null))
                        .finalScore(t.getFinalScore())
                        .submittedAt(t.getSubmittedAt())
                        .build())
                .toList();
        return ContestedSlotResponse.builder()
                .id(slot.getId())
                .trackId(slot.getTrackId())
                .trackName(trackName)
                .slotType(slot.getSlotType())
                .slotIndex(slot.getSlotIndex())
                .needsPenaltyEvaluation(slot.isNeedsPenaltyEvaluation())
                .teams(teams)
                .build();
    }

    private LocalDateTime getSubmittedAt(UUID roundId, UUID teamId) {
        if (roundId == null) {
            return null;
        }
        return submissionPublicService.getSubmissionByTeamAndRound(teamId, roundId)
                .map(s -> submissionPublicService.getSubmittedAt(s.getId()))
                .orElse(null);
    }

    private static final class SelectionState {
        final List<SelectionEntry> selections = new ArrayList<>();
        final List<ContestedEntry> contested = new ArrayList<>();
        final Set<UUID> selectedSet = new HashSet<>();
        List<UUID> selectedTeamIds = new ArrayList<>();
        String bucketScope = "GLOBAL";
        String bucketLabel = "overall";

        void addSelection(UUID teamId, FinalistSelectionMethod method, String reason, boolean needsPenalty) {
            if (selectedSet.add(teamId)) {
                selectedTeamIds.add(teamId);
                selections.add(new SelectionEntry(teamId, method, reason, needsPenalty));
            }
        }

        void addContested(UUID trackId, ContestedSlotType slotType, int slotIndex, List<Ranking> rankings) {
            contested.add(new ContestedEntry(trackId, slotType, slotIndex, rankings));
        }
    }

    private record SelectionEntry(UUID teamId, FinalistSelectionMethod method, String reason,
                                  boolean needsPenalty) {}

    private record ContestedEntry(UUID trackId, ContestedSlotType slotType, int slotIndex,
                                  List<Ranking> rankings) {}
}
