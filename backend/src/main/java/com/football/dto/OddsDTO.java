package com.football.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OddsDTO(
        Long id,
        Long matchId,
        String company,
        String sourceType,
        BigDecimal homeWin,
        BigDecimal draw,
        BigDecimal awayWin,
        BigDecimal homeWinInit,
        BigDecimal drawInit,
        BigDecimal awayWinInit,
        LocalDateTime updatedAt
) {}
