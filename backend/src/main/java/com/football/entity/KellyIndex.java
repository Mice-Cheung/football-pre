package com.football.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kelly_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KellyIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    @Column(length = 50, nullable = false)
    private String company;

    @Column(name = "home_kelly", precision = 6, scale = 4, nullable = false)
    private BigDecimal homeKelly;

    @Column(name = "draw_kelly", precision = 6, scale = 4, nullable = false)
    private BigDecimal drawKelly;

    @Column(name = "away_kelly", precision = 6, scale = 4, nullable = false)
    private BigDecimal awayKelly;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
