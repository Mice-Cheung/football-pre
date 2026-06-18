package com.football.dto;

public record MatchDetailDTO(
        MatchDTO match,
        java.util.List<LineupDTO> homeLineups,
        java.util.List<LineupDTO> awayLineups,
        java.util.List<TacticsDTO> tactics
) {}
