package com.football.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "short_name", length = 20)
    private String shortName;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(length = 50)
    private String coach;

    @Column(name = "default_formation", length = 10)
    private String defaultFormation;

    @Column(length = 50)
    private String league;

    @Column(length = 10)
    private String country;

    @Column(name = "team_color", length = 7)
    private String teamColor;
}
