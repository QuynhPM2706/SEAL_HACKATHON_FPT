package com.seal.seal_hackathon_fpt.features.judging.controller;

import com.seal.seal_hackathon_fpt.features.judging.dto.RankingResponse;
import com.seal.seal_hackathon_fpt.features.judging.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/{roundId}")
    public ResponseEntity<List<RankingResponse>>
    getRanking(
            @PathVariable Long roundId
    ) {

        return ResponseEntity.ok(
                rankingService.getRanking(roundId)
        );
    }
}