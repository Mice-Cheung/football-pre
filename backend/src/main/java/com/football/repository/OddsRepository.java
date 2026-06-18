package com.football.repository;

import com.football.entity.Odds;
import com.football.entity.enums.OddsSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OddsRepository extends JpaRepository<Odds, Long> {

    List<Odds> findByMatchId(Long matchId);

    List<Odds> findByMatchIdAndSourceType(Long matchId, OddsSourceType sourceType);

    List<Odds> findByMatchIdAndCompany(Long matchId, String company);
}
