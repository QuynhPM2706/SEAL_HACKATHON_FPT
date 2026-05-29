package com.seal.seal_hackathon_fpt.features.judging.service;

import com.seal.seal_hackathon_fpt.features.judging.dto.RankingResponse;
import com.seal.seal_hackathon_fpt.features.judging.entity.Score;
import com.seal.seal_hackathon_fpt.features.judging.entity.ScoreOverride;
import com.seal.seal_hackathon_fpt.features.judging.repository.ScoreOverrideRepository;
import com.seal.seal_hackathon_fpt.features.judging.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final ScoreRepository scoreRepository;

    private final ScoreOverrideRepository overrideRepository;

    public List<RankingResponse> getRanking(
            Long roundId
    ) {

        List<Score> scores =
                scoreRepository.findByRoundId(roundId);

        Map<Long, List<Score>> grouped =
                new HashMap<>();

        /*
            GROUP BY team
         */
        for (Score score : scores) {

            grouped
                    .computeIfAbsent(
                            score.getTeamId(),
                            k -> new ArrayList<>()
                    )
                    .add(score);
        }

        List<RankingResponse> rankings =
                new ArrayList<>();

        /*
            average
         */
        for (Map.Entry<Long, List<Score>> entry :
                grouped.entrySet()) {

            Long teamId = entry.getKey();

            List<Score> teamScores =
                    entry.getValue();

            BigDecimal total =
                    BigDecimal.ZERO;

            for (Score s : teamScores) {

                total = total.add(s.getScore());
            }

            BigDecimal average =
                    total.divide(
                            BigDecimal.valueOf(teamScores.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            /*
                check override
             */
            Optional<ScoreOverride> override =
                    overrideRepository
                            .findByTeamIdAndRoundId(
                                    teamId,
                                    roundId
                            );

            if (override.isPresent()) {

                average =
                        override.get()
                                .getOverrideScore();
            }

            rankings.add(
                    RankingResponse.builder()
                            .teamId(teamId)
                            .teamName("TEAM_" + teamId)
                            .finalScore(average)
                            .build()
            );
        }

        /*
            sort descending
         */
        rankings.sort((a, b) ->
                b.getFinalScore()
                        .compareTo(
                                a.getFinalScore()
                        )
        );

        return rankings;
    }
}