package com.seal.seal_hackathon_fpt.features.judging.repository;

import com.seal.seal_hackathon_fpt.features.judging.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository
        extends JpaRepository<Score, Long> {

    List<Score> findByTeamId(Long teamId);

    List<Score> findByRoundId(Long roundId);

    Optional<Score>
    findByJudgeIdAndTeamIdAndRoundIdAndCriterionId(
            Long judgeId,
            Long teamId,
            Long roundId,
            Long criterionId
    );
}