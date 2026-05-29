package com.seal.seal_hackathon_fpt.features.judging.repository;

import com.seal.seal_hackathon_fpt.features.judging.entity.JudgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JudgeAssignmentRepository
        extends JpaRepository<JudgeAssignment, Long> {

    List<JudgeAssignment> findByJudgeId(Long judgeId);

    List<JudgeAssignment> findByTeamId(Long teamId);

    Optional<JudgeAssignment>
    findByJudgeIdAndRoundIdAndTeamId(
            Long judgeId,
            Long roundId,
            Long teamId
    );
}