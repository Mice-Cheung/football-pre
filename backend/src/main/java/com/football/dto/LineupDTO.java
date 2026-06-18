package com.football.dto;

public record LineupDTO(
        Long id,
        Long matchId,
        Long teamId,
        String playerName,
        Integer number,
        String position,
        Boolean isStarter,
        Integer sortOrder
) {}
