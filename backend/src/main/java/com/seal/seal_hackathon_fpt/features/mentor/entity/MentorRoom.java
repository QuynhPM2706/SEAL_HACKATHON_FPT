package com.seal.seal_hackathon_fpt.features.mentor.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false, unique = true)
    private Long teamId;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}