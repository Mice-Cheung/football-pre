package com.football.entity;

import com.football.entity.enums.PlayerPosition;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lineups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @ToString.Exclude
    private Team team;

    @Column(name = "player_name", length = 50, nullable = false)
    private String playerName;

    @Column(nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PlayerPosition position;

    @Column(name = "is_starter", nullable = false)
    private Boolean isStarter;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
