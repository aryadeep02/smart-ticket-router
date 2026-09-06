package com.aryadeep.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "support_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;
}