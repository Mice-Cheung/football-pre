package com.football.controller;

import com.football.dto.MatchDTO;
import com.football.dto.MatchDetailDTO;
import com.football.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<Page<MatchDTO>> getMatches(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(matchService.getMatches(league, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatch(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<MatchDetailDTO> getMatchDetail(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchDetail(id));
    }

    @GetMapping("/leagues")
    public ResponseEntity<List<String>> getLeagues() {
        return ResponseEntity.ok(matchService.getLeagues());
    }
}
