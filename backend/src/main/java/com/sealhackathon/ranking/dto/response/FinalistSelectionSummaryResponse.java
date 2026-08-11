package com.sealhackathon.ranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalistSelectionSummaryResponse {

    private int selectedCount;
    private int targetCount;
    private boolean penaltyEvaluationRequired;
    /** PER_GROUP | PER_TRACK | GLOBAL | MANUAL */
    private String bucketScope;
    /** Human label for UI, e.g. "per group", "per track", "overall". */
    private String bucketLabel;
    private Integer topN;
}
