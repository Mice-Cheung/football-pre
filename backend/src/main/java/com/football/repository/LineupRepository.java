package com.football.repository;

import com.football.entity.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {

    List<Lineup> findByMatchIdAndTeamIdOrderBySortOrderAsc(Long matchId, Long teamId);

    List<Lineup> findByMatchIdOrderByTeamIdAscSortOrderAsc(Long matchId);
}
