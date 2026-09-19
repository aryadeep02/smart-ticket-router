package com.aryadeep.backend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final UserRepository userRepository;
        private final SecretKey secretKey;

        public JwtAuthenticationFilter(
                        UserRepository userRepository,
                        @Value("${jwt.secret}") String secret) {

                this.userRepository = userRepository;

                if (secret == null || secret.isBlank()) {
                        throw new IllegalStateException(
                                        "JWT secret must be configured");
                }

                this.secretKey = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String authorizationHeader = request.getHeader("Authorization");

                /*
                 * No Authorization header or not a Bearer token.
                 * Continue normally and let Spring Security decide
                 * whether authentication is required.
                 */
                if (authorizationHeader == null
                                || !authorizationHeader.startsWith("Bearer ")) {

                        filterChain.doFilter(request, response);
                        return;
                }

                String token = authorizationHeader.substring(7).trim();

                /*
                 * Reject an empty Bearer token.
                 */
                if (token.isEmpty()) {
                        filterChain.doFilter(request, response);
                        return;
                }

                try {

                        /*
                         * Parse and validate the JWT.
                         *
                         * This verifies:
                         * - signature
                         * - expiration
                         * - token structure
                         */
                        Claims claims = Jwts.parser()
                                        .verifyWith(secretKey)
                                        .build()
                                        .parseSignedClaims(token)
                                        .getPayload();

                        /*
                         * Our JWT subject contains the user's email.
                         */
                        String email = claims.getSubject();

                        if (email == null || email.isBlank()) {
                                filterChain.doFilter(request, response);
                                return;
                        }

                        /*
                         * Load the user from the database.
                         *
                         * We intentionally do not trust role information
                         * stored inside the JWT.
                         *
                         * This means database role changes take effect
                         * on the next authenticated request.
                         */
                        User user = userRepository
                                        .findByEmail(email)
                                        .orElse(null);

                        if (user == null) {
                                filterChain.doFilter(request, response);
                                return;
                        }

                        /*
                         * Do not overwrite an authentication created
                         * by another authentication mechanism.
                         */
                        if (SecurityContextHolder
                                        .getContext()
                                        .getAuthentication() == null) {

                                String role = "ROLE_" + user.getRole().name();

                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                user.getEmail(),
                                                null,
                                                List.of(authority));

                                SecurityContextHolder
                                                .getContext()
                                                .setAuthentication(authentication);
                        }

                } catch (Exception exception) {

                        /*
                         * Invalid / expired / malformed JWT:
                         * do not authenticate the request.
                         *
                         * Spring Security will handle the request afterward.
                         */
                        SecurityContextHolder.clearContext();
                }

                /*
                 * Continue the security filter chain.
                 */
                filterChain.doFilter(request, response);
        }
}