package com.seal.seal_hackathon_fpt.features.competition.controller;

import com.seal.seal_hackathon_fpt.features.competition.dto.PublicCompetitionResponse;
import com.seal.seal_hackathon_fpt.features.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/competitions")
@RequiredArgsConstructor
public class PublicCompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public List<PublicCompetitionResponse> getPublicCompetitions() {
        return competitionService.getPublicCompetitions();
    }

    @GetMapping("/{id}")
    public PublicCompetitionResponse getPublicCompetitionById(@PathVariable Long id) {
        return competitionService.getPublicCompetitionById(id);
    }
}