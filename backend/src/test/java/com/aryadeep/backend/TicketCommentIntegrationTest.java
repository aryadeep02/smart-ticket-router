package com.aryadeep.backend;

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.SupportTeamRepository;
import com.aryadeep.backend.repository.TicketRepository;
import com.aryadeep.backend.repository.UserRepository;
import com.aryadeep.backend.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class TicketCommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SupportTeamRepository supportTeamRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void customerShouldBeAbleToAddCommentToOwnTicket() throws Exception {

        User customer = createUser(Role.CUSTOMER);
        Ticket ticket = createTicket(customer);

        String token = jwtService.generateToken(customer.getEmail());

        String requestBody = """
                {
                    "content": "I have checked the transaction and it is still failing."
                }
                """;

        mockMvc.perform(
                post("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void customerShouldBeAbleToReadOwnTicketComments() throws Exception {

        User customer = createUser(Role.CUSTOMER);
        Ticket ticket = createTicket(customer);

        String token = jwtService.generateToken(customer.getEmail());

        String requestBody = """
                {
                    "content": "Please check my payment."
                }
                """;

        mockMvc.perform(
                post("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(
                get("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unrelatedCustomerShouldNotAccessTicketComments() throws Exception {

        User owner = createUser(Role.CUSTOMER);
        User otherCustomer = createUser(Role.CUSTOMER);

        Ticket ticket = createTicket(owner);

        String token = jwtService.generateToken(
                otherCustomer.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unrelatedCustomerShouldNotAddCommentToTicket() throws Exception {

        User owner = createUser(Role.CUSTOMER);
        User otherCustomer = createUser(Role.CUSTOMER);

        Ticket ticket = createTicket(owner);

        String token = jwtService.generateToken(
                otherCustomer.getEmail());

        String requestBody = """
                {
                    "content": "I should not be allowed to comment here."
                }
                """;

        mockMvc.perform(
                post("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void blankCommentShouldReturnBadRequest() throws Exception {

        User customer = createUser(Role.CUSTOMER);
        Ticket ticket = createTicket(customer);

        String token = jwtService.generateToken(customer.getEmail());

        String requestBody = """
                {
                    "content": ""
                }
                """;

        mockMvc.perform(
                post("/api/v1/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private User createUser(Role role) {

        String uniqueEmail =
                role.name().toLowerCase()
                        + "-" + UUID.randomUUID()
                        + "@example.com";

        User user = User.builder()
                .name("Test " + role.name())
                .email(uniqueEmail)
                .password(passwordEncoder.encode("Password@123"))
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }

    private Ticket createTicket(User customer) {

        SupportTeam team = supportTeamRepository
                .findByName("PAYMENT_SUPPORT")
                .orElseThrow();

        Ticket ticket = Ticket.builder()
                .title("Test payment ticket")
                .description("Test ticket for comment integration testing")
                .category(TicketCategory.PAYMENT)
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .customer(customer)
                .supportTeam(team)
                .aiConfidence(0.9)
                .aiClassificationStatus("COMPLETED")
                .slaDeadline(LocalDateTime.now().plusHours(12))
                .build();

        return ticketRepository.save(ticket);
    }
}