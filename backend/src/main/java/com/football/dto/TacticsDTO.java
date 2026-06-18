package com.football.dto;

public record TacticsDTO(
        Long id,
        Long matchId,
        Long teamId,
        String teamName,
        String attackStyle,
        String defenseStyle,
        String formation,
        String description,
        Integer strengthRating,
        Double possessionAvg,
        String recentForm
) {}
