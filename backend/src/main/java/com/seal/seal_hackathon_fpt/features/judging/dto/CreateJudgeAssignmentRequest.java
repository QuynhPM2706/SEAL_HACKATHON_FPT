package com.seal.seal_hackathon_fpt.features.judging.dto;

import lombok.Data;

@Data
public class CreateJudgeAssignmentRequest {

    private Long judgeId;

    private Long competitionId;

    private Long roundId;

    private Long teamId;
}