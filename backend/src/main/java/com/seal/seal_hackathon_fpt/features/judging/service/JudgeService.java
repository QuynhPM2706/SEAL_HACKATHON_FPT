package com.seal.seal_hackathon_fpt.features.judging.service;

import com.seal.seal_hackathon_fpt.features.judging.dto.CreateJudgeRequest;
import com.seal.seal_hackathon_fpt.features.judging.dto.JudgeResponse;
import com.seal.seal_hackathon_fpt.features.judging.entity.Judge;
import com.seal.seal_hackathon_fpt.features.judging.repository.JudgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeService {

    private final JudgeRepository judgeRepository;

    public JudgeResponse createJudge(
            CreateJudgeRequest request
    ) {

        Judge judge = Judge.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .isGuest(request.getIsGuest())
                .createdAt(LocalDateTime.now())
                .build();

        judgeRepository.save(judge);

        return JudgeResponse.builder()
                .id(judge.getId())
                .userId(judge.getUserId())
                .fullName(judge.getFullName())
                .isGuest(judge.getIsGuest())
                .build();
    }

    public List<Judge> getAllJudges() {
        return judgeRepository.findAll();
    }
}