package com.aryadeep.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.SupportTeamResponse;
import com.aryadeep.backend.service.SupportTeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/teams")
@Tag(
        name = "Admin Teams",
        description = "Support-team management and lookup operations"
)
@SecurityRequirement(name = "bearerAuth")
public class SupportTeamController {

    private final SupportTeamService supportTeamService;

    public SupportTeamController(
            SupportTeamService supportTeamService) {

        this.supportTeamService = supportTeamService;
    }

    @Operation(
            summary = "Get all support teams",
            description = "Returns all support teams configured in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Support teams retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only admins can access support teams"
            )
    })
    @GetMapping
    public List<SupportTeamResponse> getAllTeams() {

        return supportTeamService.getAllTeams();
    }
}