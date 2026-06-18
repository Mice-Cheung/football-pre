package com.football.repository;

import com.football.entity.Tactics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacticsRepository extends JpaRepository<Tactics, Long> {

    List<Tactics> findByMatchId(Long matchId);

    List<Tactics> findByMatchIdAndTeamId(Long matchId, Long teamId);
}
