package com.football.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.config.BrowserSimulator;
import com.football.config.FootballDataSourceProperties;
import com.football.dto.external.OkoooResponse;
import com.football.entity.KellyIndex;
import com.football.entity.Match;
import com.football.entity.Odds;
import com.football.entity.OddsHistory;
import com.football.entity.enums.OddsSourceType;
import com.football.repository.KellyRepository;
import com.football.repository.MatchRepository;
import com.football.repository.OddsHistoryRepository;
import com.football.repository.OddsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * 澳客网（okooo.com）数据抓取服务 — 浏览器模拟版
 *
 * <p>接口来源: www.okooo.com
 * <p>抓取策略:
 * <ol>
 *   <li>使用 BrowserSimulator 生成随机化浏览器请求头</li>
 *   <li>先访问澳客网足球首页获取 Cookie</li>
 *   <li>逐场请求赔率 API（模拟 AJAX）</li>
 *   <li>请求间加入高斯分布随机延迟</li>
 *   <li>Referer 链指向比赛详情页（更符合真实浏览路径）</li>
 * </ol>
 * <p>覆盖机构: Bet365, William Hill, Pinnacle, Bwin, Interwetten,
 *           Ladbrokes, SNAI, 澳门彩票 等主流博彩公司
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OkoooFetcherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BrowserSimulator browser;
    private final FootballDataSourceProperties properties;
    private final MatchRepository matchRepository;
    private final OddsRepository oddsRepository;
    private final KellyRepository kellyRepository;
    private final OddsHistoryRepository oddsHistoryRepository;

    private static final List<String> NATIONAL_COMPANIES = List.of(
            "中国体育彩票", "竞彩官方", "澳彩", "澳门", "香港马会"
    );

    /**
     * 从澳客网拉取所有比赛的赔率和凯利指数（完整浏览器模拟流程）
     */
    @Transactional
    public int fetchAndSync() {
        var op = properties.getOkooo();
        String okoooHome = "https://www.okooo.com/";
        String okoooSoccer = "https://www.okooo.com/soccer/";
        String okoooMatchPage = "https://www.okooo.com/soccer/match/";

        log.info("[澳客网] 开始拉取赔率数据 (浏览器模拟模式)");

        // ===== 阶段1: Session 预热 =====
        if (browser.isSessionWarmupEnabled()) {
            try {
                log.debug("[澳客网] 预热: 访问首页获取 Cookie");
                HttpHeaders homeHeaders = browser.buildPageVisitHeaders(null);
                restTemplate.exchange(okoooHome, HttpMethod.GET,
                        new HttpEntity<>(homeHeaders), String.class);
                browser.pageLoadDelay();

                // 再访问足球频道页（模拟真实浏览路径）
                log.debug("[澳客网] 预热: 访问足球频道");
                HttpHeaders soccerHeaders = browser.buildPageVisitHeaders(okoooHome);
                restTemplate.exchange(okoooSoccer, HttpMethod.GET,
                        new HttpEntity<>(soccerHeaders), String.class);
                browser.pageLoadDelay();

                log.debug("[澳客网] Session预热完成，Cookie已就绪");
            } catch (Exception e) {
                log.warn("[澳客网] 预热失败（不影响后续请求）: {}", e.getMessage());
            }
        }

        // ===== 阶段2: 逐场拉取赔率数据 =====
        List<Match> matches = matchRepository.findAll();
        if (matches.isEmpty()) {
            log.info("[澳客网] 无比赛记录，跳过");
            return 0;
        }

        int totalUpdated = 0;
        int totalMatches = matches.size();

        for (int i = 0; i < totalMatches; i++) {
            Match match = matches.get(i);
            try {
                int updated = fetchOddsForMatch(match, okoooMatchPage);
                if (updated > 0) totalUpdated += updated;
            } catch (Exception e) {
                log.error("[澳客网] 拉取比赛 {} 赔率失败: {}",
                        match.getMatchNo(), e.getMessage());
            }

            // 模拟人类逐场切换的间隔（高斯分布）
            if (i < totalMatches - 1) {
                browser.apiDelayGaussian();
            }
        }

        log.info("[澳客网] 同步完成: 更新 {} 条赔率记录 (共{}场比赛)",
                totalUpdated, totalMatches);
        return totalUpdated;
    }

    /**
     * 拉取单场比赛的全部赔率数据
     * 模拟从比赛详情页发起的 AJAX 请求
     */
    private int fetchOddsForMatch(Match match, String okoooMatchPage) {
        var op = properties.getOkooo();
        int count = 0;

        // 构建 Referer: 模拟从该比赛的详情页请求
        String refererUrl = okoooMatchPage + match.getMatchNo() + "/odds/";

        // 赔率接口
        String oddsUrl = String.format("%s%s?companytype=Baijia&type=1&matchId=%s",
                op.getBaseUrl(), op.getOddsPath(), match.getMatchNo());

        HttpHeaders apiHeaders = browser.buildApiRequestHeaders(refererUrl);
        HttpEntity<Void> entity = new HttpEntity<>(apiHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    oddsUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OkoooResponse result = objectMapper.readValue(
                        response.getBody(), OkoooResponse.class);

                if (result.getData() != null) {
                    for (OkoooResponse.OddsEntry entry : result.getData()) {
                        try {
                            syncOddsEntry(match, entry);
                            syncKellyEntry(match, entry);
                            count++;
                        } catch (Exception e) {
                            log.debug("[澳客网] 同步单条赔率失败: company={}",
                                    entry.getCompanyName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[澳客网] 请求赔率接口失败: match={}, error={}",
                    match.getMatchNo(), e.getMessage());
        }

        return count;
    }

    private void syncOddsEntry(Match match, OkoooResponse.OddsEntry entry) {
        String company = entry.getCompanyName();
        if (company == null || company.isBlank()) return;

        BigDecimal homeWin = parseDecimal(entry.getHomeWin());
        BigDecimal draw = parseDecimal(entry.getDraw());
        BigDecimal awayWin = parseDecimal(entry.getAwayWin());
        BigDecimal homeInit = parseDecimal(entry.getHomeWinInit());
        BigDecimal drawInit = parseDecimal(entry.getDrawInit());
        BigDecimal awayInit = parseDecimal(entry.getAwayWinInit());

        if (homeWin == null || draw == null || awayWin == null) return;

        OddsSourceType sourceType = isNationalCompany(company)
                ? OddsSourceType.NATIONAL_LOTTERY
                : OddsSourceType.INTERNATIONAL;

        List<Odds> existing = oddsRepository.findByMatchIdAndCompany(match.getId(), company);

        Odds odds;
        boolean isNew = existing.isEmpty();
        BigDecimal prevHome = null, prevDraw = null, prevAway = null;

        if (!isNew) {
            odds = existing.get(0);
            prevHome = odds.getHomeWin();
            prevDraw = odds.getDraw();
            prevAway = odds.getAwayWin();

            if (odds.getHomeWinInit() == null && homeInit != null) {
                odds.setHomeWinInit(homeInit);
                odds.setDrawInit(drawInit);
                odds.setAwayWinInit(awayInit);
            }
            odds.setHomeWin(homeWin);
            odds.setDraw(draw);
            odds.setAwayWin(awayWin);
        } else {
            odds = Odds.builder()
                    .match(match).company(company).sourceType(sourceType)
                    .homeWin(homeWin).draw(draw).awayWin(awayWin)
                    .homeWinInit(homeInit).drawInit(drawInit).awayWinInit(awayInit)
                    .build();
        }
        oddsRepository.save(odds);

        // ===== 记录赔率历史快照 =====
        recordOddsHistory(match, company, sourceType, homeWin, draw, awayWin,
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
            homeChange = homeWin.subtract(prevHome);
            drawChange = draw.subtract(prevDraw);
            awayChange = awayWin.subtract(prevAway);

            // 无变化则跳过
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

    private void syncKellyEntry(Match match, OkoooResponse.OddsEntry entry) {
        String company = entry.getCompanyName();
        BigDecimal kellyHome = parseDecimal(entry.getKellyHome());
        BigDecimal kellyDraw = parseDecimal(entry.getKellyDraw());
        BigDecimal kellyAway = parseDecimal(entry.getKellyAway());

        if (kellyHome == null || kellyDraw == null || kellyAway == null) return;

        List<KellyIndex> existing = kellyRepository.findByMatchIdAndCompany(
                match.getId(), company);

        KellyIndex kelly;
        if (!existing.isEmpty()) {
            kelly = existing.get(0);
            kelly.setHomeKelly(kellyHome);
            kelly.setDrawKelly(kellyDraw);
            kelly.setAwayKelly(kellyAway);
        } else {
            kelly = KellyIndex.builder()
                    .match(match).company(company)
                    .homeKelly(kellyHome).drawKelly(kellyDraw).awayKelly(kellyAway)
                    .build();
        }
        kellyRepository.save(kelly);
    }

    private boolean isNationalCompany(String companyName) {
        return NATIONAL_COMPANIES.stream().anyMatch(companyName::contains);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
