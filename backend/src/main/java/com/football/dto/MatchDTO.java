package com.football.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchDTO(
        Long id,
        String matchNo,
        String league,
        TeamDTO homeTeam,
        TeamDTO awayTeam,
        LocalDateTime matchDate,
        BigDecimal handicap,
        String status,
        Integer homeScore,
        Integer awayScore
) {}
