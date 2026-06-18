package com.football.service;

import com.football.config.FootballDataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 赔率数据同步定时调度器
 *
 * 每5分钟执行一次，分别从竞彩网和澳客网拉取最新数据：
 * 1. 竞彩网 (sporttery.cn) — 比赛列表 + 中国体育彩票官方赔率
 * 2. 澳客网 (okooo.com)  — 多家外国机构赔率 + 凯利指数
 *
 * 开关控制: football.datasource.sync-enabled=true
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "football.datasource.sync-enabled", havingValue = "true")
public class OddsSyncScheduler {

    private final SportteryFetcherService sportteryFetcher;
    private final OkoooFetcherService okoooFetcher;
    private final FootballDataSourceProperties properties;

    private final AtomicInteger syncCount = new AtomicInteger(0);
    private final AtomicReference<String> lastSyncTime = new AtomicReference<>("未执行");
    private final AtomicReference<String> lastSyncResult = new AtomicReference<>("-");

    /**
     * 每5分钟执行一次赔率数据同步
     */
    @Scheduled(fixedDelayString = "${football.datasource.sync-interval-ms:300000}",
               initialDelay = 15000)
    public void syncOddsData() {
        int round = syncCount.incrementAndGet();
        LocalDateTime start = LocalDateTime.now();
        log.info("========== [同步] 第 {} 轮开始 ==========", round);

        StringBuilder result = new StringBuilder();
        int totalMatches = 0;
        int totalOdds = 0;

        // 第一阶段：从竞彩网拉取比赛列表和官方赔率
        try {
            int count = sportteryFetcher.fetchAndSync();
            totalMatches = count;
            result.append(String.format("竞彩网: %d场 | ", count));
            log.info("[同步] 竞彩网完成: {} 场比赛", count);
        } catch (Exception e) {
            result.append("竞彩网: 失败 | ");
            log.error("[同步] 竞彩网拉取异常", e);
        }

        // 第二阶段：从澳客网拉取多家机构赔率和凯利指数
        try {
            int count = okoooFetcher.fetchAndSync();
            totalOdds = count;
            result.append(String.format("澳客网: %d条", count));
            log.info("[同步] 澳客网完成: {} 条赔率记录", count);
        } catch (Exception e) {
            result.append("澳客网: 失败");
            log.error("[同步] 澳客网拉取异常", e);
        }

        lastSyncTime.set(start.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")));
        lastSyncResult.set(String.format("比赛%d场 赔率%d条", totalMatches, totalOdds));

        log.info("========== [同步] 第 {} 轮完成: {} (耗时 {}ms) ==========",
                round, result,
                java.time.Duration.between(start, LocalDateTime.now()).toMillis());
    }

    /**
     * 获取同步状态（供监控接口使用）
     */
    public SyncStatus getStatus() {
        return new SyncStatus(
                properties.isSyncEnabled(),
                syncCount.get(),
                lastSyncTime.get(),
                lastSyncResult.get()
        );
    }

    public record SyncStatus(boolean enabled, int totalRounds, String lastSyncTime, String lastResult) {}
}
