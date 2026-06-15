package com.seal.seal_hackathon_fpt.features.competition.controller;

import com.seal.seal_hackathon_fpt.features.competition.dto.CreateCompetitionRequest;
import com.seal.seal_hackathon_fpt.features.competition.dto.UpdateCompetitionRequest;
import com.seal.seal_hackathon_fpt.features.competition.entity.Competition;
import com.seal.seal_hackathon_fpt.features.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @PostMapping
    public Competition create(@RequestBody CreateCompetitionRequest request) {
        return competitionService.create(request);
    }

    @GetMapping
    public List<Competition> getAll() {
        return competitionService.getAll();
    }

    @GetMapping("/{id}")
    public Competition getById(@PathVariable Long id) {
        return competitionService.getById(id);
    }

    @PutMapping("/{id}")
    public Competition update(@PathVariable Long id, @RequestBody UpdateCompetitionRequest request) {
        return competitionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        competitionService.delete(id);
    }
}