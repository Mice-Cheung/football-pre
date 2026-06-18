package com.football.entity;

import com.football.entity.enums.OddsSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "odds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Odds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    @Column(length = 50, nullable = false)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private OddsSourceType sourceType;

    @Column(name = "home_win", precision = 5, scale = 2, nullable = false)
    private BigDecimal homeWin;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal draw;

    @Column(name = "away_win", precision = 5, scale = 2, nullable = false)
    private BigDecimal awayWin;

    @Column(name = "home_win_init", precision = 5, scale = 2)
    private BigDecimal homeWinInit;

    @Column(name = "draw_init", precision = 5, scale = 2)
    private BigDecimal drawInit;

    @Column(name = "away_win_init", precision = 5, scale = 2)
    private BigDecimal awayWinInit;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
