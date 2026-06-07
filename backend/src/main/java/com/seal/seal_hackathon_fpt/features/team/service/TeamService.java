package com.seal.seal_hackathon_fpt.features.team.service;

import com.seal.seal_hackathon_fpt.features.team.entity.Team;
import com.seal.seal_hackathon_fpt.features.team.entity.TeamMember;
import com.seal.seal_hackathon_fpt.features.team.repository.TeamMemberRepository;
import com.seal.seal_hackathon_fpt.features.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository memberRepository;

    public Team createTeam(Team team, Long creatorUserId) {
        Team savedTeam = teamRepository.save(team);

        addMemberToTeam(savedTeam.getId(), creatorUserId, true);

        return savedTeam;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public List<TeamMember> getMembersByTeamId(Long teamId) {
        return memberRepository.findByTeamId(teamId);
    }

    public TeamMember addMemberToTeam(Long teamId, Long userId, boolean isLeader) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }

        if (memberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("User is already in this team");
        }

        long memberCount = memberRepository.countByTeamId(teamId);

        if (memberCount >= 5) {
            throw new RuntimeException("Team cannot have more than 5 members");
        }

        if (isLeader && memberRepository.existsByTeamIdAndIsLeaderTrue(teamId)) {
            throw new RuntimeException("This team already has a leader");
        }

        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(userId)
                .isLeader(isLeader)
                .joinedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(member);
    }

    public void removeMember(Long teamId, Long userId) {
        TeamMember member = memberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (Boolean.TRUE.equals(member.getIsLeader())) {
            throw new RuntimeException("Cannot remove the leader");
        }

        memberRepository.delete(member);
    }
}