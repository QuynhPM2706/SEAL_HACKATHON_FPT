package com.seal.seal_hackathon_fpt.features.judging.repository;

import com.seal.seal_hackathon_fpt.features.judging.entity.ScoreOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreOverrideRepository
        extends JpaRepository<ScoreOverride, Long> {

    Optional<ScoreOverride>
    findByTeamIdAndRoundId(
            Long teamId,
            Long roundId
    );
}