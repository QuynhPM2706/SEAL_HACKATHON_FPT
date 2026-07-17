package com.sealhackathon.judging.dto.response;

import com.sealhackathon.event.domain.enums.AssignmentScope;
import com.sealhackathon.submission.domain.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeScoringAssignmentResponse {

    private UUID teamId;
    private String teamName;
    private UUID roundId;
    private String roundName;
    private UUID eventId;
    private String eventName;
    private UUID trackId;
    private String trackName;
    private UUID groupId;
    private String groupName;
    private AssignmentScope assignmentScope;
    private UUID submissionId;
    private SubmissionStatus submissionStatus;
    private LocalDateTime submittedAt;
    private String scoringStatus;
    private LocalDateTime scoringDeadline;
    private boolean conflictOfInterest;
    private String conflictReason;
    private boolean scoringAllowed;
    private String scoringDeniedReason;
    private boolean hasOpenScoreReview;
    private UUID openScoreReviewId;
}
