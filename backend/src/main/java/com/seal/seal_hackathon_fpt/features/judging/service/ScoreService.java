package com.seal.seal_hackathon_fpt.features.judging.service;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateScoreRequest;
import com.seal.seal_hackathon_fpt.features.judging.entity.Score;
import com.seal.seal_hackathon_fpt.features.judging.repository.JudgeAssignmentRepository;
import com.seal.seal_hackathon_fpt.features.judging.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;

    private final JudgeAssignmentRepository assignmentRepository;

    public Score createScore(
            CreateScoreRequest request
    ) {

        boolean assigned = assignmentRepository
                .findByJudgeIdAndRoundIdAndTeamId(
                        request.getJudgeId(),
                        request.getRoundId(),
                        request.getTeamId()
                )
                .isPresent();

        if (!assigned) {
            throw new RuntimeException(
                    "Judge not assigned"
            );
        }

        boolean duplicate = scoreRepository
                .findByJudgeIdAndTeamIdAndRoundIdAndCriterionId(
                        request.getJudgeId(),
                        request.getTeamId(),
                        request.getRoundId(),
                        request.getCriterionId()
                )
                .isPresent();

        if (duplicate) {
            throw new RuntimeException(
                    "Duplicate score"
            );
        }

        Score score = Score.builder()
                .judgeId(request.getJudgeId())
                .teamId(request.getTeamId())
                .roundId(request.getRoundId())
                .criterionId(request.getCriterionId())
                .score(request.getScore())
                .comment(request.getComment())
                .status("PENDING_REVIEW")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return scoreRepository.save(score);
    }
}