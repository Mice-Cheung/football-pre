package com.football.service;

import com.football.dto.MatchDTO;
import com.football.dto.MatchDetailDTO;
import com.football.dto.TeamDTO;
import com.football.entity.Match;
import com.football.exception.ResourceNotFoundException;
import com.football.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamService teamService;

    public Page<MatchDTO> getMatches(String league, String status, Pageable pageable) {
        Page<Match> matchPage;
        if (league != null && !league.isBlank() && status != null && !status.isBlank()) {
            matchPage = matchRepository.findByLeagueAndStatus(league,
                    com.football.entity.enums.MatchStatus.valueOf(status.toUpperCase()), pageable);
        } else if (league != null && !league.isBlank()) {
            matchPage = matchRepository.findByLeague(league, pageable);
        } else if (status != null && !status.isBlank()) {
            matchPage = matchRepository.findByStatus(
                    com.football.entity.enums.MatchStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            matchPage = matchRepository.findAll(pageable);
        }
        return matchPage.map(this::toDTO);
    }

    public MatchDTO getMatchById(Long id) {
        Match match = matchRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
        return toDTO(match);
    }

    public List<String> getLeagues() {
        return matchRepository.findDistinctLeagues();
    }

    public MatchDetailDTO getMatchDetail(Long id) {
        Match match = matchRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
        return new MatchDetailDTO(
                toDTO(match),
                teamService.getLineupsByMatchAndTeam(id, match.getHomeTeam().getId()),
                teamService.getLineupsByMatchAndTeam(id, match.getAwayTeam().getId()),
                List.of()
        );
    }

    private MatchDTO toDTO(Match match) {
        return new MatchDTO(
                match.getId(),
                match.getMatchNo(),
                match.getLeague(),
                teamService.toDTO(match.getHomeTeam()),
                teamService.toDTO(match.getAwayTeam()),
                match.getMatchDate(),
                match.getHandicap(),
                match.getStatus().name(),
                match.getHomeScore(),
                match.getAwayScore()
        );
    }
}
