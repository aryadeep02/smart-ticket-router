package com.aryadeep.backend;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;
import com.aryadeep.backend.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void validationErrorShouldReturnStandardErrorContract()
            throws Exception {

        User customer = createCustomer();

        String token = jwtService.generateToken(
                customer.getEmail());

        String requestBody = """
                {
                    "title": "",
                    "description": ""
                }
                """;

        mockMvc.perform(
                post("/api/v1/tickets")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/tickets"))
                .andExpect(jsonPath("$.timestamp")
                        .exists())
                .andExpect(jsonPath("$.errors.title")
                        .value("Title is required"))
                .andExpect(jsonPath("$.errors.description")
                        .value("Description is required"));
    }

    @Test
    void forbiddenErrorShouldReturnStandardErrorContract()
            throws Exception {

        User customer = createCustomer();

        String token = jwtService.generateToken(
                customer.getEmail());

        mockMvc.perform(
                get("/api/v1/admin/users")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error")
                        .value("FORBIDDEN"))
                .andExpect(jsonPath("$.message")
                        .exists())
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/admin/users"))
                .andExpect(jsonPath("$.timestamp")
                        .exists());
    }

    private User createCustomer() {

        String email =
                "exception-test-"
                        + UUID.randomUUID()
                        + "@test.com";

        User customer = User.builder()
                .name("Exception Test Customer")
                .email(email)
                .password(
                        passwordEncoder.encode("Password@123"))
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();

        return userRepository.save(customer);
    }
}