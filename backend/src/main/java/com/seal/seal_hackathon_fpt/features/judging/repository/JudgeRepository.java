package com.seal.seal_hackathon_fpt.features.judging.repository;

import com.seal.seal_hackathon_fpt.features.judging.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JudgeRepository extends JpaRepository<Judge, Long> {

    Optional<Judge> findByUserId(Long userId);
}