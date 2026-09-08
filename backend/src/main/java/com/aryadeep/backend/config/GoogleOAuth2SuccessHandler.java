package com.aryadeep.backend.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.service.JwtService;
import com.aryadeep.backend.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GoogleOAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    public GoogleOAuth2SuccessHandler(
            UserService userService,
            JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauthUser = oauthToken.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Google account email is required");
            return;
        }

        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        User user = userService.findOrCreateGoogleUser(
                name,
                email);

        String token = jwtService.generateToken(
                user.getEmail());

        String frontendUrl =
                "http://localhost:5173/oauth2/callback?token="
                        + URLEncoder.encode(
                                token,
                                StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(
                request,
                response,
                frontendUrl);
    }
}