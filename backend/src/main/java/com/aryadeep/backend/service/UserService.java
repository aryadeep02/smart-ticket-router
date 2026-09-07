package com.aryadeep.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aryadeep.backend.dto.LoginResponse;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.exception.EmailAlreadyExistsException;
import com.aryadeep.backend.exception.InvalidCredentialsException;
import com.aryadeep.backend.repository.SupportTeamRepository;
import com.aryadeep.backend.repository.UserRepository;
import com.aryadeep.backend.entity.SupportTeam;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final SupportTeamRepository supportTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            SupportTeamRepository supportTeamRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.supportTeamRepository = supportTeamRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User registerUser(String name, String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .build();

        return userRepository.save(user);
    }

    public User createUserWithRole(
            String name,
            String email,
            String password,
            Role role,
            Long supportTeamId) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        SupportTeam supportTeam = null;

        if (role == Role.AGENT) {

            if (supportTeamId == null) {
                throw new IllegalStateException(
                        "Support team is required for an AGENT");
            }

            supportTeam = supportTeamRepository
                    .findById(supportTeamId)
                    .orElseThrow(() -> new RuntimeException(
                            "Support team not found"));
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .supportTeam(supportTeam)
                .build();

        return userRepository.save(user);
    }

    public LoginResponse loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }
}