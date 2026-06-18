package com.football.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "football.datasource")
public class FootballDataSourceProperties {

    /** 定时同步开关 */
    private boolean syncEnabled = false;

    /** 定时任务间隔（毫秒），默认5分钟 */
    private long syncIntervalMs = 300_000;

    private final Sporttery sporttery = new Sporttery();
    private final Okooo okooo = new Okooo();

    @Data
    public static class Sporttery {
        private String baseUrl = "https://webapi.sporttery.cn";
        private String matchListPath = "/gateway/jc/football/getMatchInfoV1.qry";
        private int dayRange = 3;
    }

    @Data
    public static class Okooo {
        private String baseUrl = "https://www.okooo.com";
        private String oddsPath = "/soccer/match/odds/ajax/";
        private String kellyPath = "/soccer/match/kelly/ajax/";
    }

    @Data
    public static class Browser {
        /** 是否启用 Session 预热 */
        private boolean sessionWarmup = true;
        /** 请求间最小延迟（毫秒） */
        private int minDelayMs = 500;
        /** 请求间最大延迟（毫秒） */
        private int maxDelayMs = 3000;
        /** 页面加载等待时间（毫秒） */
        private int pageLoadDelayMs = 2000;
    }

    private final Browser browser = new Browser();
}
