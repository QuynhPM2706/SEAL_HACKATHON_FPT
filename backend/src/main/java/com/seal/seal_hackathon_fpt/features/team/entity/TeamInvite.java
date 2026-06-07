package com.seal.seal_hackathon_fpt.features.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_member_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(name = "team_id")
    private Long teamId;

    private String track;

    @Column(name = "to_email")
    private String toEmail;

    @Column(name = "from_user_id")
    private Long fromUserId;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}