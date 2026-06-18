package com.football.service;

import com.football.dto.*;
import com.football.dto.OddsTraceabilityDTO.CompanyOddsHistory;
import com.football.dto.OddsTraceabilityDTO.OddsHistoryPoint;
import com.football.entity.KellyIndex;
import com.football.entity.Odds;
import com.football.entity.OddsHistory;
import com.football.entity.enums.OddsSourceType;
import com.football.repository.KellyRepository;
import com.football.repository.OddsHistoryRepository;
import com.football.repository.OddsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OddsService {

    private final OddsRepository oddsRepository;
    private final KellyRepository kellyRepository;
    private final OddsHistoryRepository oddsHistoryRepository;

    /** 赔率变化告警阈值: 变化超过此值触发告警 */
    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("0.10");
    /** 严重告警阈值: 变化超过此值触发严重告警 */
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("0.20");

    public List<OddsDTO> getOddsByMatch(Long matchId) {
        return oddsRepository.findByMatchId(matchId).stream()
                .map(this::toDTO)
                .toList();
    }

    public OddsComparisonDTO getOddsComparison(Long matchId) {
        List<OddsDTO> nationalOdds = oddsRepository
                .findByMatchIdAndSourceType(matchId, OddsSourceType.NATIONAL_LOTTERY)
                .stream().map(this::toDTO).toList();

        List<OddsDTO> internationalOdds = oddsRepository
                .findByMatchIdAndSourceType(matchId, OddsSourceType.INTERNATIONAL)
                .stream().map(this::toDTO).toList();

        BigDecimal homeWinDiff = calcMaxDiff(nationalOdds, internationalOdds, OddsDTO::homeWin);
        BigDecimal drawDiff = calcMaxDiff(nationalOdds, internationalOdds, OddsDTO::draw);
        BigDecimal awayWinDiff = calcMaxDiff(nationalOdds, internationalOdds, OddsDTO::awayWin);

        return new OddsComparisonDTO(nationalOdds, internationalOdds, homeWinDiff, drawDiff, awayWinDiff);
    }

    public List<KellyDTO> getKellyByMatch(Long matchId) {
        return kellyRepository.findByMatchId(matchId).stream()
                .map(this::toDTO)
                .toList();
    }

    private BigDecimal calcMaxDiff(List<OddsDTO> national, List<OddsDTO> international,
                                    java.util.function.Function<OddsDTO, BigDecimal> getter) {
        if (national.isEmpty() || international.isEmpty()) return BigDecimal.ZERO;
        BigDecimal nationalAvg = national.stream().map(getter)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(national.size()), 4, RoundingMode.HALF_UP);
        BigDecimal maxDiff = BigDecimal.ZERO;
        for (OddsDTO intl : international) {
            BigDecimal diff = getter.apply(intl).subtract(nationalAvg).abs();
            if (diff.compareTo(maxDiff) > 0) {
                maxDiff = diff;
            }
        }
        return maxDiff;
    }

    private OddsDTO toDTO(Odds odds) {
        return new OddsDTO(
                odds.getId(),
                odds.getMatch().getId(),
                odds.getCompany(),
                odds.getSourceType().name(),
                odds.getHomeWin(),
                odds.getDraw(),
                odds.getAwayWin(),
                odds.getHomeWinInit(),
                odds.getDrawInit(),
                odds.getAwayWinInit(),
                odds.getUpdatedAt()
        );
    }

    private KellyDTO toDTO(KellyIndex kelly) {
        return new KellyDTO(
                kelly.getId(),
                kelly.getMatch().getId(),
                kelly.getCompany(),
                kelly.getHomeKelly(),
                kelly.getDrawKelly(),
                kelly.getAwayKelly(),
                kelly.getUpdatedAt()
        );
    }

    // ==================== 赔率追溯 ====================

    /**
     * 获取某场比赛的完整赔率追溯信息
     * 包含所有机构的历史变化 + 显著变化告警
     */
    public OddsTraceabilityDTO getOddsTraceability(Long matchId) {
        List<OddsHistory> allHistory = oddsHistoryRepository.findByMatchIdOrderByRecordedAtAsc(matchId);

        if (allHistory.isEmpty()) {
            // 无历史数据，返回空结果
            return new OddsTraceabilityDTO(List.of(), List.of(), ALERT_THRESHOLD);
        }

        // 按机构分组
        Map<String, List<OddsHistory>> grouped = allHistory.stream()
                .collect(Collectors.groupingBy(OddsHistory::getCompany, LinkedHashMap::new, Collectors.toList()));

        List<CompanyOddsHistory> companies = new ArrayList<>();
        for (Map.Entry<String, List<OddsHistory>> entry : grouped.entrySet()) {
            companies.add(buildCompanyHistory(entry.getKey(), entry.getValue()));
        }

        // 生成告警
        List<OddsAlertDTO> alerts = buildAlerts(matchId, allHistory);

        return new OddsTraceabilityDTO(companies, alerts, ALERT_THRESHOLD);
    }

    /**
     * 获取某场比赛指定来源类型的赔率追溯（国内/海外）
     */
    public OddsTraceabilityDTO getOddsTraceabilityByType(Long matchId, String sourceType) {
        OddsSourceType type = OddsSourceType.valueOf(sourceType);
        List<OddsHistory> allHistory = oddsHistoryRepository.findByMatchIdAndSourceType(matchId, type);

        if (allHistory.isEmpty()) {
            return new OddsTraceabilityDTO(List.of(), List.of(), ALERT_THRESHOLD);
        }

        Map<String, List<OddsHistory>> grouped = allHistory.stream()
                .collect(Collectors.groupingBy(OddsHistory::getCompany, LinkedHashMap::new, Collectors.toList()));

        List<CompanyOddsHistory> companies = new ArrayList<>();
        for (Map.Entry<String, List<OddsHistory>> entry : grouped.entrySet()) {
            companies.add(buildCompanyHistory(entry.getKey(), entry.getValue()));
        }

        List<OddsAlertDTO> alerts = buildAlerts(matchId, allHistory);

        return new OddsTraceabilityDTO(companies, alerts, ALERT_THRESHOLD);
    }

    /**
     * 构建单个机构的赔率历史
     */
    private CompanyOddsHistory buildCompanyHistory(String company, List<OddsHistory> history) {
        OddsHistory first = history.get(0);
        OddsHistory last = history.get(history.size() - 1);

        List<OddsHistoryPoint> points = history.stream()
                .map(h -> new OddsHistoryPoint(
                        h.getId(),
                        h.getRecordedAt(),
                        h.getHomeWin(),
                        h.getDraw(),
                        h.getAwayWin(),
                        h.getHomeChange(),
                        h.getDrawChange(),
                        h.getAwayChange(),
                        h.getIsInitial()))
                .toList();

        BigDecimal homeInit = first.getHomeWin();
        BigDecimal drawInit = first.getDraw();
        BigDecimal awayInit = first.getAwayWin();

        BigDecimal homeLive = last.getHomeWin();
        BigDecimal drawLive = last.getDraw();
        BigDecimal awayLive = last.getAwayWin();

        BigDecimal homeTotalChange = homeLive.subtract(homeInit);
        BigDecimal drawTotalChange = drawLive.subtract(drawInit);
        BigDecimal awayTotalChange = awayLive.subtract(awayInit);

        return new CompanyOddsHistory(
                company,
                first.getSourceType().name(),
                points,
                homeInit, drawInit, awayInit,
                homeLive, drawLive, awayLive,
                homeTotalChange, drawTotalChange, awayTotalChange);
    }

    /**
     * 生成显著变化告警列表
     */
    private List<OddsAlertDTO> buildAlerts(Long matchId, List<OddsHistory> history) {
        List<OddsAlertDTO> alerts = new ArrayList<>();

        for (OddsHistory h : history) {
            if (Boolean.TRUE.equals(h.getIsInitial())) continue;
            if (h.getHomeChange() == null && h.getDrawChange() == null && h.getAwayChange() == null) continue;

            alerts.addAll(buildAlertForDimension(matchId, h, "home",
                    h.getHomeChange(), getPrevOdds(history, h, "home"), h.getHomeWin()));
            alerts.addAll(buildAlertForDimension(matchId, h, "draw",
                    h.getDrawChange(), getPrevOdds(history, h, "draw"), h.getDraw()));
            alerts.addAll(buildAlertForDimension(matchId, h, "away",
                    h.getAwayChange(), getPrevOdds(history, h, "away"), h.getAwayWin()));
        }

        // 去重: 同一公司同一时间只保留最高级别告警
        return alerts.stream()
                .sorted(Comparator.comparing(OddsAlertDTO::severity).reversed()
                        .thenComparing(OddsAlertDTO::changeAmount, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private List<OddsAlertDTO> buildAlertForDimension(Long matchId, OddsHistory h, String dim,
                                                       BigDecimal change, BigDecimal oldOdds,
                                                       BigDecimal newOdds) {
        if (change == null || change.abs().compareTo(ALERT_THRESHOLD) < 0) {
            return List.of();
        }

        String severity = change.abs().compareTo(CRITICAL_THRESHOLD) >= 0 ? "CRITICAL" : "WARN";
        BigDecimal changePercent = oldOdds != null && oldOdds.compareTo(BigDecimal.ZERO) != 0
                ? change.divide(oldOdds, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        OddsAlertDTO alert = new OddsAlertDTO(
                h.getId(),
                matchId,
                h.getCompany(),
                h.getSourceType().name(),
                dim,
                severity,
                oldOdds != null ? oldOdds : newOdds,
                newOdds,
                change,
                changePercent,
                h.getRecordedAt()
        );

        return List.of(alert);
    }

    private BigDecimal getPrevOdds(List<OddsHistory> history, OddsHistory current, String dim) {
        int idx = history.indexOf(current);
        if (idx <= 0) return null;
        OddsHistory prev = history.get(idx - 1);
        return switch (dim) {
            case "home" -> prev.getHomeWin();
            case "draw" -> prev.getDraw();
            case "away" -> prev.getAwayWin();
            default -> null;
        };
    }
}
