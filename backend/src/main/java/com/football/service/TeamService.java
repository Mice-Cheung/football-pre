package com.football.service;

import com.football.dto.LineupDTO;
import com.football.dto.TeamDTO;
import com.football.entity.Lineup;
import com.football.entity.Team;
import com.football.exception.ResourceNotFoundException;
import com.football.repository.LineupRepository;
import com.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final LineupRepository lineupRepository;

    public TeamDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
        return toDTO(team);
    }

    public List<TeamDTO> getTeamsByLeague(String league) {
        return teamRepository.findByLeague(league).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LineupDTO> getLineupsByMatchAndTeam(Long matchId, Long teamId) {
        return lineupRepository.findByMatchIdAndTeamIdOrderBySortOrderAsc(matchId, teamId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LineupDTO> getLineupsByMatch(Long matchId) {
        return lineupRepository.findByMatchIdOrderByTeamIdAscSortOrderAsc(matchId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TeamDTO toDTO(Team team) {
        if (team == null) return null;
        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getNameEn(),
                team.getShortName(),
                team.getLogoUrl(),
                team.getCoach(),
                team.getDefaultFormation(),
                team.getLeague(),
                team.getCountry(),
                team.getTeamColor()
        );
    }

    private LineupDTO toDTO(Lineup lineup) {
        return new LineupDTO(
                lineup.getId(),
                lineup.getMatch().getId(),
                lineup.getTeam().getId(),
                lineup.getPlayerName(),
                lineup.getNumber(),
                lineup.getPosition().name(),
                lineup.getIsStarter(),
                lineup.getSortOrder()
        );
    }
}
