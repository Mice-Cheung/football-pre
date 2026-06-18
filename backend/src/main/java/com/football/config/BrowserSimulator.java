package com.football.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 浏览器行为模拟器 — 核心反爬引擎
 *
 * <p>通过模拟真实人类浏览行为降低被拦截概率：
 * <ul>
 *   <li>User-Agent 轮换池（10+ 个主流浏览器）</li>
 *   <li>请求间随机延迟（模拟人类阅读/点击间隔）</li>
 *   <li>浏览器指纹头（Sec-Fetch-*, Accept-*, 语言偏好）</li>
 *   <li>Session 预热（先访问主页获取 Cookie，再请求 API）</li>
 *   <li>Referer 链追踪（请求来源页面）</li>
 * </ul>
 */
@Slf4j
@Component
public class BrowserSimulator {

    private final FootballDataSourceProperties.Browser config;

    public BrowserSimulator(FootballDataSourceProperties properties) {
        this.config = properties.getBrowser();
        log.info("[浏览器模拟] 初始化完成: session预热={}, 延迟范围={}-{}ms, 页面加载={}ms",
                config.isSessionWarmup(), config.getMinDelayMs(),
                config.getMaxDelayMs(), config.getPageLoadDelayMs());
    }

    /**
     * User-Agent 轮换池
     * 覆盖 Chrome/Firefox/Edge on Windows/macOS 主流版本
     */
    private static final List<String> USER_AGENTS = List.of(
            // Chrome 120 - Windows 10
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            // Chrome 120 - Windows 11
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.130 Safari/537.36",
            // Chrome 119 - macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.199 Safari/537.36",
            // Chrome 118 - Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.118 Safari/537.36",
            // Firefox 121 - Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            // Firefox 120 - macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:120.0) Gecko/20100101 Firefox/120.0",
            // Edge 120 - Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
            // Chrome 117 - Linux
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.5938.132 Safari/537.36",
            // Safari 16 - macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Safari/605.1.15",
            // Chrome 120 - Windows (alternative build)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.110 Safari/537.36"
    );

    /**
     * Accept-Language 轮换池（中文用户环境）
     */
    private static final List<String> ACCEPT_LANGUAGES = List.of(
            "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
            "zh-CN,zh;q=0.9",
            "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4"
    );

    /**
     * Accept 轮换池（根据请求类型选择）
     */
    private static final String ACCEPT_HTML = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String ACCEPT_JSON = "application/json, text/javascript, */*; q=0.01";
    private static final String ACCEPT_ALL  = "*/*";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 构建"模拟浏览器首页访问"的请求头
     * 适用于先访问目标网站主页，获取 Cookie 后再请求 API
     *
     * @param refererPage 当前所"在"的页面 URL（通常为主页）
     */
    public HttpHeaders buildPageVisitHeaders(String refererPage) {
        HttpHeaders headers = new HttpHeaders();
        String ua = randomUA();

        headers.set(HttpHeaders.USER_AGENT, ua);
        headers.set(HttpHeaders.ACCEPT, ACCEPT_HTML);
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, randomAcceptLanguage());
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set("Cache-Control", "max-age=0");
        headers.set("Connection", "keep-alive");
        // Sec-Fetch 系列 — 浏览器的安全策略头
        headers.set("Sec-Fetch-Dest", "document");
        headers.set("Sec-Fetch-Mode", "navigate");
        headers.set("Sec-Fetch-Site", "none");
        headers.set("Sec-Fetch-User", "?1");
        headers.set("Upgrade-Insecure-Requests", "1");
        // DNT（Do Not Track）
        if (RANDOM.nextBoolean()) {
            headers.set("DNT", "1");
        }

        if (refererPage != null && !refererPage.isBlank()) {
            headers.set(HttpHeaders.REFERER, refererPage);
        }

        return headers;
    }

    /**
     * 构建"模拟浏览器 XHR/AJAX 请求"的请求头
     * 适用于 API 数据接口请求（带 Referer 链）
     *
     * @param refererPage 当前所在页面 URL（例如比赛列表页）
     */
    public HttpHeaders buildApiRequestHeaders(String refererPage) {
        HttpHeaders headers = new HttpHeaders();
        String ua = randomUA();

        headers.set(HttpHeaders.USER_AGENT, ua);
        headers.set(HttpHeaders.ACCEPT, ACCEPT_JSON);
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, randomAcceptLanguage());
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set("Cache-Control", "no-cache");
        headers.set("Connection", "keep-alive");
        // X-Requested-With 标识 AJAX 请求
        headers.set("X-Requested-With", "XMLHttpRequest");
        // Sec-Fetch 系列 — 标识为 fetch/XHR
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "same-origin");

        if (refererPage != null && !refererPage.isBlank()) {
            headers.set(HttpHeaders.REFERER, refererPage);
            headers.set("Origin", extractOrigin(refererPage));
        }

        return headers;
    }

    /**
     * 模拟人类"阅读页面"的延迟（毫秒级随机）
     * 在请求不同页面之间调用，模拟用户浏览行为
     */
    public void humanDelay() {
        sleep(config.getMinDelayMs() + 300, config.getMaxDelayMs() + 500);
    }

    /**
     * 模拟 API 请求之间的短延迟
     */
    public void apiDelay() {
        sleep(config.getMinDelayMs(), config.getMaxDelayMs());
    }

    /**
     * 模拟页面加载后的等待时间
     */
    public void pageLoadDelay() {
        sleep(config.getPageLoadDelayMs() - 500, config.getPageLoadDelayMs() + 2000);
    }

    /**
     * 带高斯分布的 API 延迟（更接近真实人类行为）
     * 大部分请求集中在均值附近，少数请求会有明显偏移
     */
    public void apiDelayGaussian() {
        int mean = (config.getMinDelayMs() + config.getMaxDelayMs()) / 2;
        int stddev = (config.getMaxDelayMs() - config.getMinDelayMs()) / 3;
        double gaussian = RANDOM.nextGaussian();
        int delay = (int) (mean + gaussian * stddev);
        delay = Math.max(config.getMinDelayMs(), Math.min(config.getMaxDelayMs() + 500, delay));
        sleep(delay, delay);
    }

    /** 是否启用 Session 预热 */
    public boolean isSessionWarmupEnabled() {
        return config.isSessionWarmup();
    }

    // ========== 私有工具方法 ==========

    private void sleep(int minMs, int maxMs) {
        int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String randomUA() {
        return USER_AGENTS.get(RANDOM.nextInt(USER_AGENTS.size()));
    }

    private String randomAcceptLanguage() {
        return ACCEPT_LANGUAGES.get(RANDOM.nextInt(ACCEPT_LANGUAGES.size()));
    }

    private String extractOrigin(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getProtocol() + "://" + u.getHost();
        } catch (Exception e) {
            return url;
        }
    }
}
