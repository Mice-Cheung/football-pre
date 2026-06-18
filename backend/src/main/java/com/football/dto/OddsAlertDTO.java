package com.football.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 赔率显著变化告警
 */
public record OddsAlertDTO(
        Long id,
        Long matchId,
        String company,
        String sourceType,
        /** 告警类型: home/draw/away */
        String alertType,
        /** 告警级别: WARN(0.10-0.20) / CRITICAL(>0.20) */
        String severity,
        /** 变化前赔率 */
        BigDecimal oldOdds,
        /** 变化后赔率 */
        BigDecimal newOdds,
        /** 变化幅度 */
        BigDecimal changeAmount,
        /** 变化百分比 */
        BigDecimal changePercent,
        /** 变化时间 */
        LocalDateTime changeTime
) {}
