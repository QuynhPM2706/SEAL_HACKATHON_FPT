package com.seal.seal_hackathon_fpt.features.judging.controller;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateScoreRequest;
import com.seal.seal_hackathon_fpt.features.judging.entity.Score;
import com.seal.seal_hackathon_fpt.features.judging.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    public ResponseEntity<Score> createScore(
            @RequestBody CreateScoreRequest request
    ) {

        return ResponseEntity.ok(
                scoreService.createScore(request)
        );
    }
}