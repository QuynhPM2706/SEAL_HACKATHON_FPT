package com.seal.seal_hackathon_fpt.features.judging.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RankingResponse {

    private Long teamId;

    private String teamName;

    private BigDecimal finalScore;
}