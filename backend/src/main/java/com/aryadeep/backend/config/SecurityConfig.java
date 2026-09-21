package com.aryadeep.backend.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
            ObjectMapper objectMapper)
            throws Exception {

        /*
         * Handles unauthenticated requests.
         */
        AuthenticationEntryPoint authenticationEntryPoint =
                (request, response, authException) -> {

                    writeErrorResponse(
                            response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "UNAUTHORIZED",
                            "Authentication required",
                            request.getRequestURI(),
                            objectMapper);
                };

        /*
         * Handles authenticated users who do not
         * have permission to access a resource.
         */
        AccessDeniedHandler accessDeniedHandler =
                (request, response, accessDeniedException) -> {

                    String message =
                            accessDeniedException.getMessage() != null
                                    ? accessDeniedException.getMessage()
                                    : "You do not have permission to perform this action";

                    writeErrorResponse(
                            response,
                            HttpServletResponse.SC_FORBIDDEN,
                            "FORBIDDEN",
                            message,
                            request.getRequestURI(),
                            objectMapper);
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
                         * Swagger / OpenAPI
                         */
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
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
                                "/api/v1/admin/dashboard")
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

        /*
         * Run JWT authentication before Spring's
         * UsernamePasswordAuthenticationFilter.
         */
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * Creates the standard security-layer error response.
     *
     * IOException is the checked exception supported by
     * Spring Security's AuthenticationEntryPoint and
     * AccessDeniedHandler callbacks.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String error,
            String message,
            String path,
            ObjectMapper objectMapper)
            throws IOException {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now());

        body.put(
                "status",
                status);

        body.put(
                "error",
                error);

        body.put(
                "message",
                message);

        body.put(
                "path",
                path);

        response.setStatus(status);

        response.setCharacterEncoding("UTF-8");

        response.setContentType("application/json");

        response.getWriter().write(
                objectMapper.writeValueAsString(body));
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