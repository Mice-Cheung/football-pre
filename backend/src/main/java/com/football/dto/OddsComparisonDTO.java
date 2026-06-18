package com.football.dto;

import java.math.BigDecimal;
import java.util.List;

public record OddsComparisonDTO(
        List<OddsDTO> nationalOdds,
        List<OddsDTO> internationalOdds,
        BigDecimal homeWinDiff,
        BigDecimal drawDiff,
        BigDecimal awayWinDiff
) {}
