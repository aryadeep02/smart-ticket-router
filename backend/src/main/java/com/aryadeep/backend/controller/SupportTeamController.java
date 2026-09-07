package com.aryadeep.backend.controller;

import com.aryadeep.backend.dto.SupportTeamResponse;
import com.aryadeep.backend.service.SupportTeamService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/teams")
public class SupportTeamController {

    private final SupportTeamService supportTeamService;

    public SupportTeamController(
            SupportTeamService supportTeamService) {
        this.supportTeamService = supportTeamService;
    }

    @GetMapping
    public List<SupportTeamResponse> getAllTeams() {
        return supportTeamService.getAllTeams();
    }
}