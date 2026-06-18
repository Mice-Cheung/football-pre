package com.football.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.config.BrowserSimulator;
import com.football.config.FootballDataSourceProperties;
import com.football.dto.external.SportteryResponse;
import com.football.dto.external.SportteryResponse.BusinessDateGroup;
import com.football.dto.external.SportteryResponse.MatchInfo;
import com.football.entity.Match;
import com.football.entity.Odds;
import com.football.entity.OddsHistory;
import com.football.entity.Team;
import com.football.entity.enums.MatchStatus;
import com.football.entity.enums.OddsSourceType;
import com.football.repository.MatchRepository;
import com.football.repository.OddsHistoryRepository;
import com.football.repository.OddsRepository;
import com.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 竞彩网（中国体育彩票官方）数据抓取服务
 *
 * <p>接口: webapi.sporttery.cn/gateway/jc/football/getMatchCalculatorV1.qry
 * <p>抓取策略: 浏览器模拟 → Session预热 → 嵌套JSON展平 → 入库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SportteryFetcherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BrowserSimulator browser;
    private final FootballDataSourceProperties properties;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final OddsRepository oddsRepository;
    private final OddsHistoryRepository oddsHistoryRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 从竞彩网拉取比赛列表和赔率数据
     */
    @Transactional
    public int fetchAndSync() {
        var sp = properties.getSporttery();
        String sportteryHome = "https://www.sporttery.cn/";
        String sportteryJcPage = "https://www.sporttery.cn/jc/index.html";

        log.info("[竞彩网] 开始拉取比赛数据");

        // ===== 阶段1: Session 预热 =====
        if (browser.isSessionWarmupEnabled()) {
            try {
                log.debug("[竞彩网] 预热: 访问首页");
                HttpHeaders homeHeaders = browser.buildPageVisitHeaders(null);
                restTemplate.exchange(sportteryHome, HttpMethod.GET,
                        new HttpEntity<>(homeHeaders), String.class);
                browser.pageLoadDelay();

                log.debug("[竞彩网] 预热: 访问JC频道");
                HttpHeaders jcHeaders = browser.buildPageVisitHeaders(sportteryHome);
                restTemplate.exchange(sportteryJcPage, HttpMethod.GET,
                        new HttpEntity<>(jcHeaders), String.class);
                browser.pageLoadDelay();
                log.debug("[竞彩网] Session预热完成");
            } catch (Exception e) {
                log.warn("[竞彩网] 预热失败（不影响后续）: {}", e.getMessage());
            }
        }

        // ===== 阶段2: 调用 API =====
        // 端点: getMatchCalculatorV1.qry (真实可用端点)
        // 参数: poolCode=hhad,had (胜平负+让球胜平负)
        String url = String.format("%s/gateway/jc/football/getMatchCalculatorV1.qry?poolCode=hhad,had&channel=c",
                sp.getBaseUrl());

        log.debug("[竞彩网] API URL: {}", url);

        HttpHeaders apiHeaders = browser.buildApiRequestHeaders(sportteryJcPage);
        HttpEntity<Void> entity = new HttpEntity<>(apiHeaders);

        try {
            browser.humanDelay();

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[竞彩网] API请求失败: status={}", response.getStatusCode());
                return 0;
            }

            String body = response.getBody();
            log.debug("[竞彩网] 响应长度: {} 字符", body.length());

            SportteryResponse result = objectMapper.readValue(body, SportteryResponse.class);

            if (!"0".equals(result.getErrorCode())) {
                log.warn("[竞彩网] API返回错误: code={}, msg={}",
                        result.getErrorCode(), result.getErrorMessage());
                return 0;
            }

            // 展平嵌套结构: businessDate -> subMatchList
            List<MatchInfo> allMatches = flattenMatches(result);
            if (allMatches.isEmpty()) {
                log.info("[竞彩网] 无比赛数据");
                return 0;
            }

            int total = allMatches.size();
            int count = 0;

            for (int i = 0; i < total; i++) {
                MatchInfo info = allMatches.get(i);
                try {
                    syncMatch(info);
                    count++;
                } catch (Exception e) {
                    log.error("[竞彩网] 同步单场失败: matchNo={}, error={}",
                            info.getMatchNumStr(), e.getMessage());
                }
                if (i < total - 1) {
                    browser.apiDelayGaussian();
                }
            }

            log.info("[竞彩网] 同步完成: {}/{} 场比赛", count, total);
            return count;
        } catch (Exception e) {
            log.error("[竞彩网] 拉取失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 展平竞彩网嵌套数据结构
     * matchInfoList[].businessDate -> subMatchList[] -> 比赛
     */
    private List<MatchInfo> flattenMatches(SportteryResponse response) {
        List<MatchInfo> flat = new ArrayList<>();
        if (response.getValue() == null || response.getValue().getMatchInfoList() == null) {
            return flat;
        }
        for (BusinessDateGroup group : response.getValue().getMatchInfoList()) {
            if (group.getSubMatchList() != null) {
                for (MatchInfo m : group.getSubMatchList()) {
                    m.setBusinessDate(group.getBusinessDate());
                    flat.add(m);
                }
            }
        }
        return flat;
    }

    /**
     * 同步单场比赛数据
     */
    private void syncMatch(MatchInfo info) {
        String matchNo = info.getMatchNumStr();
        if (matchNo == null || matchNo.isBlank()) return;

        String league = coalesce(info.getLeagueAbbName(), info.getLeagueAllName(), "?");
        String homeName = coalesce(info.getHomeTeamAbbName(), info.getHomeTeamAllName(), "?");
        String awayName = coalesce(info.getAwayTeamAbbName(), info.getAwayTeamAllName(), "?");

        Team homeTeam = findOrCreateTeam(homeName, league);
        Team awayTeam = findOrCreateTeam(awayName, league);
        LocalDateTime matchTime = parseMatchTime(info.getMatchDate(), info.getMatchTime());

        // 让球数从 hhad 中提取
        String goalLine = null;
        if (info.getHhad() != null) {
            goalLine = info.getHhad().getGoalLine();
        }
        BigDecimal handicap = parseBigDecimal(goalLine);

        Optional<Match> existing = matchRepository.findByMatchNo(matchNo);
        Match match;
        if (existing.isPresent()) {
            match = existing.get();
            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);
            match.setLeague(league);
            match.setMatchDate(matchTime);
            match.setHandicap(handicap);
        } else {
            match = Match.builder()
                    .matchNo(matchNo)
                    .league(league)
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .matchDate(matchTime)
                    .handicap(handicap)
                    .status(MatchStatus.PENDING)
                    .build();
        }
        match = matchRepository.save(match);

        // 同步赔率
        syncOdds(match, info);

        log.debug("[竞彩网] 同步: {} {} vs {} ({})",
                matchNo, homeTeam.getName(), awayTeam.getName(), league);
    }

    /**
     * 同步中国体育彩票官方赔率（胜平负 + 让球胜平负）
     */
    private void syncOdds(Match match, MatchInfo info) {
        // 1. 胜平负赔率 (had)
        if (info.getHad() != null) {
            BigDecimal h = parseBigDecimal(info.getHad().getH());
            BigDecimal d = parseBigDecimal(info.getHad().getD());
            BigDecimal a = parseBigDecimal(info.getHad().getA());
            if (h != null && d != null && a != null) {
                upsertOdds(match, "中国体育彩票(胜平负)", OddsSourceType.NATIONAL_LOTTERY, h, d, a);
            }
        }

        // 2. 让球胜平负赔率 (hhad)
        if (info.getHhad() != null) {
            BigDecimal hh = parseBigDecimal(info.getHhad().getH());
            BigDecimal hd = parseBigDecimal(info.getHhad().getD());
            BigDecimal ha = parseBigDecimal(info.getHhad().getA());
            if (hh != null && hd != null && ha != null) {
                upsertOdds(match, "中国体育彩票(让球)", OddsSourceType.NATIONAL_LOTTERY, hh, hd, ha);
            }
        }
    }

    private void upsertOdds(Match match, String company, OddsSourceType type,
                            BigDecimal homeWin, BigDecimal draw, BigDecimal awayWin) {
        List<Odds> existingOdds = oddsRepository.findByMatchIdAndCompany(match.getId(), company);

        Odds odds;
        boolean isNew = existingOdds.isEmpty();
        BigDecimal prevHome = null, prevDraw = null, prevAway = null;

        if (!isNew) {
            odds = existingOdds.get(0);
            // 记录当前值作为"上一条"
            prevHome = odds.getHomeWin();
            prevDraw = odds.getDraw();
            prevAway = odds.getAwayWin();

            if (odds.getHomeWinInit() == null) {
                odds.setHomeWinInit(homeWin);
                odds.setDrawInit(draw);
                odds.setAwayWinInit(awayWin);
            }
            odds.setHomeWin(homeWin);
            odds.setDraw(draw);
            odds.setAwayWin(awayWin);
        } else {
            odds = Odds.builder()
                    .match(match)
                    .company(company)
                    .sourceType(type)
                    .homeWin(homeWin)
                    .draw(draw)
                    .awayWin(awayWin)
                    .homeWinInit(homeWin)
                    .drawInit(draw)
                    .awayWinInit(awayWin)
                    .build();
        }
        oddsRepository.save(odds);

        // ===== 记录赔率历史快照 =====
        recordOddsHistory(match, company, type, homeWin, draw, awayWin,
                prevHome, prevDraw, prevAway, isNew);
    }

    /**
     * 记录赔率历史快照，当赔率发生变更时写入
     */
    private void recordOddsHistory(Match match, String company, OddsSourceType type,
                                    BigDecimal homeWin, BigDecimal draw, BigDecimal awayWin,
                                    BigDecimal prevHome, BigDecimal prevDraw, BigDecimal prevAway,
                                    boolean isNew) {
        BigDecimal homeChange = BigDecimal.ZERO;
        BigDecimal drawChange = BigDecimal.ZERO;
        BigDecimal awayChange = BigDecimal.ZERO;

        if (!isNew && prevHome != null && prevDraw != null && prevAway != null) {
            // 计算变化
            homeChange = homeWin.subtract(prevHome);
            drawChange = draw.subtract(prevDraw);
            awayChange = awayWin.subtract(prevAway);

            // 如果没有任何变化，跳过记录
            if (homeChange.compareTo(BigDecimal.ZERO) == 0
                    && drawChange.compareTo(BigDecimal.ZERO) == 0
                    && awayChange.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
        }

        OddsHistory history = OddsHistory.builder()
                .match(match)
                .company(company)
                .sourceType(type)
                .homeWin(homeWin)
                .draw(draw)
                .awayWin(awayWin)
                .homeChange(homeChange)
                .drawChange(drawChange)
                .awayChange(awayChange)
                .isInitial(isNew)
                .recordedAt(java.time.LocalDateTime.now())
                .build();
        oddsHistoryRepository.save(history);
    }

    // ========== 工具方法 ==========

    private Team findOrCreateTeam(String name, String league) {
        return teamRepository.findByName(name)
                .orElseGet(() -> {
                    Team t = Team.builder()
                            .name(name).nameEn(name)
                            .shortName(name.length() > 4 ? name.substring(0, 4) : name)
                            .league(league).defaultFormation("4-4-2")
                            .build();
                    return teamRepository.save(t);
                });
    }

    private LocalDateTime parseMatchTime(String dateStr, String timeStr) {
        try {
            if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now().plusDays(1);
            if (timeStr == null || timeStr.isBlank()) timeStr = "20:00:00";
            // 截取 HH:mm 部分
            timeStr = timeStr.length() >= 5 ? timeStr.substring(0, 5) : timeStr;
            return LocalDateTime.of(
                    LocalDate.parse(dateStr, DATE_FMT),
                    LocalTime.parse(timeStr));
        } catch (Exception e) {
            log.warn("[竞彩网] 时间解析失败: date={}, time={}", dateStr, timeStr);
            return LocalDateTime.now().plusDays(1);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String coalesce(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "?";
    }
}
