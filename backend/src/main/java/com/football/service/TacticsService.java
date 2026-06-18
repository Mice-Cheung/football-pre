package com.football.service;

import com.football.dto.TacticsDTO;
import com.football.entity.Tactics;
import com.football.repository.TacticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TacticsService {

    private final TacticsRepository tacticsRepository;

    public List<TacticsDTO> getTacticsByMatch(Long matchId) {
        return tacticsRepository.findByMatchId(matchId).stream()
                .map(this::toDTO)
                .toList();
    }

    public TacticsDTO getTacticsByMatchAndTeam(Long matchId, Long teamId) {
        List<Tactics> list = tacticsRepository.findByMatchIdAndTeamId(matchId, teamId);
        return list.isEmpty() ? null : toDTO(list.get(0));
    }

    private TacticsDTO toDTO(Tactics tactics) {
        return new TacticsDTO(
                tactics.getId(),
                tactics.getMatch().getId(),
                tactics.getTeam().getId(),
                tactics.getTeam().getName(),
                tactics.getAttackStyle(),
                tactics.getDefenseStyle(),
                tactics.getFormation(),
                tactics.getDescription(),
                tactics.getStrengthRating(),
                tactics.getPossessionAvg(),
                tactics.getRecentForm()
        );
    }
}
