package com.football.dto;

public record TeamDTO(
        Long id,
        String name,
        String nameEn,
        String shortName,
        String logoUrl,
        String coach,
        String defaultFormation,
        String league,
        String country,
        String teamColor
) {}
