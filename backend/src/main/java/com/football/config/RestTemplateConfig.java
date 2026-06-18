package com.football.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import static org.apache.hc.core5.util.TimeValue.of;
import static org.apache.hc.core5.util.TimeValue.ofSeconds;

/**
 * RestTemplate 配置 — 模拟真实浏览器行为
 * <p>
 * 核心能力：
 * 1. Apache HttpClient5 作为底层引擎（Cookie自动管理、连接池）
 * 2. 跳过自签名SSL证书验证
 * 3. 连接池复用（模拟浏览器 keep-alive 连接）
 * 4. 全局共享 CookieStore（同一会话内跨请求携带 Cookie）
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public CookieStore cookieStore() {
        return new BasicCookieStore();
    }

    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        try {
            // 信任所有SSL证书（数据源可能使用自签名证书）
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);

            return PoolingHttpClientConnectionManagerBuilder.create()
                    .setTlsSocketStrategy(tlsStrategy)
                    .setMaxConnTotal(20)                          // 最大连接数
                    .setMaxConnPerRoute(10)                        // 每个路由最大连接数
                    .setDefaultSocketConfig(SocketConfig.custom()
                            .setSoTimeout(ofSeconds(30))            // 读超时
                            .setTcpNoDelay(true)                   // 禁用 Nagle 算法
                            .build())
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(ofSeconds(10))
                            .setSocketTimeout(ofSeconds(30))
                            .build())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("初始化 SSL 连接池失败", e);
        }
    }

    @Bean
    public CloseableHttpClient httpClient(CookieStore cookieStore,
                                          PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(ofSeconds(10))
                .setResponseTimeout(ofSeconds(30))
                // 自动跟随重定向
                .setRedirectsEnabled(true)
                // 不自动携带 Referer（由业务层手动控制）
                .setCircularRedirectsAllowed(false)
                .build();

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)           // 全局共享 Cookie 罐
                .setConnectionManagerShared(true)
                // 连接保活策略：空闲连接30秒后清理
                .evictExpiredConnections()
                .evictIdleConnections(of(30, TimeUnit.SECONDS))
                // 自动重试（网络瞬时故障）
                .setRetryStrategy(new org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy(2,
                        of(1, TimeUnit.SECONDS)))
                .build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
