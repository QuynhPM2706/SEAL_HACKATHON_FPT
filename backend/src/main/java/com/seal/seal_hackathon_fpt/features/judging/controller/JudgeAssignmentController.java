package com.seal.seal_hackathon_fpt.features.judging.controller;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateJudgeAssignmentRequest;
import com.seal.seal_hackathon_fpt.features.judging.entity.JudgeAssignment;
import com.seal.seal_hackathon_fpt.features.judging.service.JudgeAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class JudgeAssignmentController {

    private final JudgeAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<JudgeAssignment> createAssignment(
            @RequestBody CreateJudgeAssignmentRequest request
    ) {

        return ResponseEntity.ok(
                assignmentService.createAssignment(request)
        );
    }
}