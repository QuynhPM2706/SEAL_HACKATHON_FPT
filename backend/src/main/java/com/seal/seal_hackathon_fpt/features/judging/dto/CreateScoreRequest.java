package com.seal.seal_hackathon_fpt.features.judging.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateScoreRequest {

    private Long judgeId;

    private Long teamId;

    private Long roundId;

    private Long criterionId;

    private BigDecimal score;

    private String comment;
}