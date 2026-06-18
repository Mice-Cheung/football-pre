package com.football.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KellyDTO(
        Long id,
        Long matchId,
        String company,
        BigDecimal homeKelly,
        BigDecimal drawKelly,
        BigDecimal awayKelly,
        LocalDateTime updatedAt
) {}
