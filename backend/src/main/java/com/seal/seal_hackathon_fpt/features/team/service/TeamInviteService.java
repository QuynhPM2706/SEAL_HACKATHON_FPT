package com.seal.seal_hackathon_fpt.features.team.service;

import com.seal.seal_hackathon_fpt.features.team.entity.TeamInvite;
import com.seal.seal_hackathon_fpt.features.team.repository.TeamInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInviteService {

    private final TeamInviteRepository inviteRepository;
    private final TeamService teamService;

    public TeamInvite sendInvite(Long teamId, Long inviterId, String email) {
        TeamInvite invite = TeamInvite.builder()
                .teamId(teamId)
                .fromUserId(inviterId)
                .toEmail(email)
                .token(UUID.randomUUID().toString())
                .track("TEAM_INVITE")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return inviteRepository.save(invite);
    }

    public void acceptInvite(Long inviteId, Long currentUserId, String currentUserEmail) {
        TeamInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invite is not pending");
        }

        if (!invite.getToEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new RuntimeException("You are not allowed to accept this invitation");
        }

        invite.setStatus("ACCEPTED");
        inviteRepository.save(invite);

        teamService.addMemberToTeam(invite.getTeamId(), currentUserId, false);
    }
}