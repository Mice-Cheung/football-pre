package com.football.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 赔率追溯完整响应: 包含每条历史记录和告警列表
 */
public record OddsTraceabilityDTO(
        /** 按机构分组的历史记录 */
        List<CompanyOddsHistory> companies,
        /** 显著变化告警 */
        List<OddsAlertDTO> alerts,
        /** 告警阈值 */
        BigDecimal alertThreshold
) {

    /**
     * 单个机构的所有历史赔率快照
     */
    public record CompanyOddsHistory(
            String company,
            String sourceType,
            List<OddsHistoryPoint> history,
            /** 初盘赔率 */
            BigDecimal homeWinInit,
            BigDecimal drawInit,
            BigDecimal awayWinInit,
            /** 实时赔率 */
            BigDecimal homeWinLive,
            BigDecimal drawLive,
            BigDecimal awayWinLive,
            /** 整体变化 */
            BigDecimal homeWinTotalChange,
            BigDecimal drawTotalChange,
            BigDecimal awayWinTotalChange
    ) {}

    /**
     * 单个时间点的赔率快照
     */
    public record OddsHistoryPoint(
            Long id,
            LocalDateTime recordedAt,
            BigDecimal homeWin,
            BigDecimal draw,
            BigDecimal awayWin,
            BigDecimal homeChange,
            BigDecimal drawChange,
            BigDecimal awayChange,
            Boolean isInitial
    ) {}
}
