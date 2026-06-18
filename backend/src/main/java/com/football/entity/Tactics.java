package com.football.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tactics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tactics {

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

    @Column(name = "attack_style", length = 50)
    private String attackStyle;

    @Column(name = "defense_style", length = 50)
    private String defenseStyle;

    @Column(length = 10)
    private String formation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "strength_rating")
    private Integer strengthRating;

    @Column(name = "possession_avg", precision = 4, scale = 1)
    private Double possessionAvg;

    @Column(name = "recent_form", length = 50)
    private String recentForm;
}
