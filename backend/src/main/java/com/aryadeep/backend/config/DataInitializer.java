package com.aryadeep.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.SupportTeamRepository;
import com.aryadeep.backend.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            SupportTeamRepository supportTeamRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            initializeSupportTeams(supportTeamRepository);

            initializeAdmin(userRepository, passwordEncoder);
        };
    }

    private void initializeSupportTeams(
            SupportTeamRepository supportTeamRepository) {

        createTeam(
                supportTeamRepository,
                "ACCOUNT_SUPPORT",
                "Handles account and profile related issues."
        );

        createTeam(
                supportTeamRepository,
                "PAYMENT_SUPPORT",
                "Handles payment, billing and refund related issues."
        );

        createTeam(
                supportTeamRepository,
                "TECHNICAL_SUPPORT",
                "Handles technical and application issues."
        );

        createTeam(
                supportTeamRepository,
                "DELIVERY_SUPPORT",
                "Handles delivery and order related issues."
        );

        createTeam(
                supportTeamRepository,
                "GENERAL_SUPPORT",
                "Handles general and uncategorized issues."
        );
    }

    private void createTeam(
            SupportTeamRepository repository,
            String name,
            String description) {

        if (!repository.existsByName(name)) {

            SupportTeam team = SupportTeam.builder()
                    .name(name)
                    .description(description)
                    .build();

            repository.save(team);
        }
    }

    private void initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        if (!userRepository.existsByEmail("admin@example.com")) {

            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
        }
    }
}