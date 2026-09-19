package com.aryadeep.backend.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler)
            throws Exception {

        AuthenticationEntryPoint authenticationEntryPoint =
                (request, response, authException) -> {

                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED);

                    response.setContentType("application/json");

                    response.getWriter().write(
                            "{\"message\":\"Unauthorized\"}");
                };

        AccessDeniedHandler accessDeniedHandler =
                (request, response, accessDeniedException) -> {

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

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                authenticationEntryPoint)
                        .accessDeniedHandler(
                                accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public application endpoints
                         */
                        .requestMatchers(
                                "/api/v1/health",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password")
                        .permitAll()

                        /*
                         * OAuth2 endpoints
                         */
                        .requestMatchers(
                                "/oauth2/**",
                                "/login",
                                "/login/**")
                        .permitAll()

                        /*
                         * Admin user creation
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/admin/users")
                        .hasRole("ADMIN")

                        /*
                         * Ticket creation
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/tickets")
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN")

                        /*
                         * Ticket assignment
                         */
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/tickets/*/assign")
                        .hasRole("ADMIN")

                        /*
                         * Ticket status update
                         */
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/tickets/*/status")
                        .hasAnyRole(
                                "AGENT",
                                "ADMIN")

                        /*
                         * Admin users
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/users")
                        .hasRole("ADMIN")

                        /*
                         * Admin support teams
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/teams")
                        .hasRole("ADMIN")

                        /*
                         * Admin dashboard
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/tickets/admin/summary")
                        .hasRole("ADMIN")

                        /*
                         * Everything else requires authentication.
                         */
                        .anyRequest()
                        .authenticated())

                /*
                 * Google OAuth2 login
                 */
                .oauth2Login(oauth2 -> oauth2

                        .successHandler(
                                googleOAuth2SuccessHandler)

                        /*
                         * Do not let Spring hide the real OAuth
                         * failure behind /login?error.
                         */
                        .failureHandler(
                                (request, response, exception) -> {

                                    logger.error(
                                            "Google OAuth2 login failed",
                                            exception);

                                    response.sendRedirect(
                                            "http://localhost:5173/login"
                                                    + "?error=google-login-failed");
                                }))

                /*
                 * JWT authentication is used instead of
                 * HTTP Basic authentication.
                 */
                .httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

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

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration);

        return source;
    }
}