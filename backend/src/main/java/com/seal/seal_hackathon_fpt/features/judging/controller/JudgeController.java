package com.seal.seal_hackathon_fpt.features.judging.controller;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateJudgeRequest;
import com.seal.seal_hackathon_fpt.features.judging.dto.JudgeResponse;
import com.seal.seal_hackathon_fpt.features.judging.entity.Judge;
import com.seal.seal_hackathon_fpt.features.judging.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judges")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @PostMapping
    public ResponseEntity<JudgeResponse> createJudge(
            @RequestBody CreateJudgeRequest request
    ) {

        return ResponseEntity.ok(
                judgeService.createJudge(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<Judge>> getAllJudges() {

        return ResponseEntity.ok(
                judgeService.getAllJudges()
        );
    }
}