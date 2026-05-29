package com.seal.seal_hackathon_fpt.features.judging.service;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateJudgeAssignmentRequest;
import com.seal.seal_hackathon_fpt.features.judging.entity.JudgeAssignment;
import com.seal.seal_hackathon_fpt.features.judging.repository.JudgeAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JudgeAssignmentService {

    private final JudgeAssignmentRepository assignmentRepository;

    public JudgeAssignment createAssignment(
            CreateJudgeAssignmentRequest request
    ) {

        boolean exists = assignmentRepository
                .findByJudgeIdAndRoundIdAndTeamId(
                        request.getJudgeId(),
                        request.getRoundId(),
                        request.getTeamId()
                )
                .isPresent();

        if (exists) {
            throw new RuntimeException(
                    "Assignment already exists"
            );
        }

        JudgeAssignment assignment =
                JudgeAssignment.builder()
                        .judgeId(request.getJudgeId())
                        .competitionId(request.getCompetitionId())
                        .roundId(request.getRoundId())
                        .teamId(request.getTeamId())
                        .assignedAt(LocalDateTime.now())
                        .build();

        return assignmentRepository.save(assignment);
    }
}