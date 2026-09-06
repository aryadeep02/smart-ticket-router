package com.aryadeep.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryadeep.backend.entity.SupportTeam;

public interface SupportTeamRepository extends JpaRepository<SupportTeam, Long> {

    Optional<SupportTeam> findByName(String name);

    boolean existsByName(String name);
}