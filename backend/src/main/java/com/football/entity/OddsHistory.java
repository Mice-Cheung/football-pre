package com.football.entity;

import com.football.entity.enums.OddsSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 赔率历史快照 — 每次从外部数据源拉取赔率时，若与上次不同则记录一笔。
 * 支持「赔率追溯」：查看从初盘到实时的每一次赔率变化。
 */
@Entity
@Table(name = "odds_history", indexes = {
        @Index(name = "idx_oh_match_company", columnList = "match_id, company, recorded_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OddsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    /** 博彩公司名称 */
    @Column(length = 50, nullable = false)
    private String company;

    /** 数据来源: 国内彩票 / 海外机构 */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private OddsSourceType sourceType;

    /** 当前主胜赔率 */
    @Column(name = "home_win", precision = 5, scale = 2, nullable = false)
    private BigDecimal homeWin;

    /** 当前平局赔率 */
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal draw;

    /** 当前客胜赔率 */
    @Column(name = "away_win", precision = 5, scale = 2, nullable = false)
    private BigDecimal awayWin;

    // ===== 变化幅度（与上一条记录的差值）=====

    @Column(name = "home_change", precision = 5, scale = 2)
    private BigDecimal homeChange;

    @Column(name = "draw_change", precision = 5, scale = 2)
    private BigDecimal drawChange;

    @Column(name = "away_change", precision = 5, scale = 2)
    private BigDecimal awayChange;

    /** 记录时间 */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /** 是否为初盘（该机构首次记录） */
    @Column(name = "is_initial", nullable = false)
    private Boolean isInitial;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (isInitial == null) {
            isInitial = false;
        }
    }
}
