package com.football.controller;

import com.football.dto.TacticsDTO;
import com.football.service.TacticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tactics")
@RequiredArgsConstructor
public class TacticsController {

    private final TacticsService tacticsService;

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<TacticsDTO>> getTacticsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(tacticsService.getTacticsByMatch(matchId));
    }

    @GetMapping("/match/{matchId}/team/{teamId}")
    public ResponseEntity<TacticsDTO> getTacticsByMatchAndTeam(
            @PathVariable Long matchId, @PathVariable Long teamId) {
        return ResponseEntity.ok(tacticsService.getTacticsByMatchAndTeam(matchId, teamId));
    }
}
