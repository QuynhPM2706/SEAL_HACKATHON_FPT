package com.seal.seal_hackathon_fpt.features.judging.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudgeResponse {

    private Long id;

    private Long userId;

    private String fullName;

    private Boolean isGuest;
}