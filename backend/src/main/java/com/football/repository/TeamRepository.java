package com.football.repository;

import com.football.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByLeague(String league);

    List<Team> findByNameContaining(String name);

    java.util.Optional<Team> findByName(String name);
}
