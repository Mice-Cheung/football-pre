package com.football.repository;

import com.football.entity.KellyIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KellyRepository extends JpaRepository<KellyIndex, Long> {

    List<KellyIndex> findByMatchId(Long matchId);

    List<KellyIndex> findByMatchIdAndCompany(Long matchId, String company);
}
