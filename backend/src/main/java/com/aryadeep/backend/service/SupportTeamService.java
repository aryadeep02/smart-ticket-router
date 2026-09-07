package com.aryadeep.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aryadeep.backend.dto.SupportTeamResponse;
// import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.repository.SupportTeamRepository;

@Service
public class SupportTeamService {

    private final SupportTeamRepository supportTeamRepository;

    public SupportTeamService(
            SupportTeamRepository supportTeamRepository) {
        this.supportTeamRepository = supportTeamRepository;
    }

    public List<SupportTeamResponse> getAllTeams() {

        return supportTeamRepository.findAll()
                .stream()
                .map(team -> new SupportTeamResponse(
                        team.getId(),
                        team.getName(),
                        team.getDescription()
                ))
                .toList();
    }
}