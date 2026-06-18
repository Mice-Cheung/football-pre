package com.football.controller;

import com.football.dto.LineupDTO;
import com.football.dto.TeamDTO;
import com.football.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getTeamsByLeague(@RequestParam String league) {
        return ResponseEntity.ok(teamService.getTeamsByLeague(league));
    }

    @GetMapping("/lineups/match/{matchId}")
    public ResponseEntity<List<LineupDTO>> getLineupsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(teamService.getLineupsByMatch(matchId));
    }

    @GetMapping("/lineups/match/{matchId}/team/{teamId}")
    public ResponseEntity<List<LineupDTO>> getLineupsByMatchAndTeam(
            @PathVariable Long matchId, @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getLineupsByMatchAndTeam(matchId, teamId));
    }
}
