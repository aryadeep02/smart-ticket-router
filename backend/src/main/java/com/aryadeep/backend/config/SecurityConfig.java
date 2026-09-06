package com.aryadeep.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        AuthenticationEntryPoint authenticationEntryPoint =
                (request, response, authException) -> {

                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED
                    );

                    response.setContentType("application/json");

                    response.getWriter().write(
                            "{\"message\":\"Unauthorized\"}"
                    );
                };

        AccessDeniedHandler accessDeniedHandler =
                (request, response, accessDeniedException) -> {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN
                    );

                    response.setContentType("application/json");

                    response.getWriter().write(
                            "{\"message\":\"Forbidden\"}"
                    );
                };

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/v1/health",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/admin/users"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/tickets"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/tickets/*/status"
                        ).hasAnyRole(
                                "AGENT",
                                "ADMIN"
                        )

                        .anyRequest().authenticated()
                )

                .httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}