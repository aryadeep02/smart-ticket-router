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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler) throws Exception {

                AuthenticationEntryPoint authenticationEntryPoint = (request, response, authException) -> {

                        response.setStatus(
                                        HttpServletResponse.SC_UNAUTHORIZED);

                        response.setContentType("application/json");

                        response.getWriter().write(
                                        "{\"message\":\"Unauthorized\"}");
                };

                AccessDeniedHandler accessDeniedHandler = (request, response, accessDeniedException) -> {

                        response.setStatus(
                                        HttpServletResponse.SC_FORBIDDEN);

                        response.setContentType("application/json");

                        response.getWriter().write(
                                        "{\"message\":\"Forbidden\"}");
                };

                http

                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> {
                                })

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                authenticationEntryPoint)
                                                .accessDeniedHandler(
                                                                accessDeniedHandler))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/api/v1/health",
                                                                "/api/v1/auth/register",
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/verify-email",
                                                                "/api/v1/auth/resend-verification")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/v1/admin/users")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/v1/tickets")
                                                .hasAnyRole("CUSTOMER", "ADMIN")
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/v1/tickets/*/assign")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/v1/tickets/*/status")
                                                .hasAnyRole(
                                                                "AGENT",
                                                                "ADMIN")
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/admin/users")
                                                .hasRole("ADMIN")
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/admin/teams")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/tickets/admin/summary")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(googleOAuth2SuccessHandler))
                                .httpBasic(AbstractHttpConfigurer::disable);

                http.addFilterBefore(
                                jwtAuthenticationFilter,
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                List.of("http://localhost:5173"));

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}