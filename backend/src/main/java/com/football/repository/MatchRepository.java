package com.football.repository;

import com.football.entity.Match;
import com.football.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Page<Match> findByStatus(MatchStatus status, Pageable pageable);

    Page<Match> findByLeague(String league, Pageable pageable);

    Page<Match> findByLeagueAndStatus(String league, MatchStatus status, Pageable pageable);

    @Query("SELECT DISTINCT m.league FROM Match m ORDER BY m.league")
    List<String> findDistinctLeagues();

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.id = :id")
    java.util.Optional<Match> findByIdWithTeams(@Param("id") Long id);

    List<Match> findByMatchDateBetweenOrderByMatchDateAsc(
            java.time.LocalDateTime start, java.time.LocalDateTime end);

    java.util.Optional<Match> findByMatchNo(String matchNo);
}
