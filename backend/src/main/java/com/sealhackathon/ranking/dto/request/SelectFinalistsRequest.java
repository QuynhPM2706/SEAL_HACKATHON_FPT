package com.sealhackathon.ranking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectFinalistsRequest {

    /** AUTO (default) = Top N by rankings; MANUAL = explicit teamIds. */
    @Builder.Default
    private SelectionMode mode = SelectionMode.AUTO;

    /**
     * Top N per bucket (group if any teams have groups, else track, else global).
     * Required for AUTO when not relying on hidden auto-cutoff config.
     */
    @Min(1)
    @Max(50)
    private Integer topN;

    /** Team IDs to advance when mode = MANUAL. */
    private List<UUID> teamIds;

    public enum SelectionMode {
        AUTO,
        MANUAL
    }
}
