package com.aryadeep.backend;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class SecurityIntegrationTest {

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

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                /*
                 * Do not delete users or tickets here.
                 *
                 * Existing tickets may reference existing users through
                 * foreign-key relationships.
                 *
                 * Each test creates uniquely named users instead.
                 */
        }

        @Test
        void unauthenticatedRequestToProtectedEndpointShouldReturn401()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/auth/me"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void publicHealthEndpointShouldBeAccessibleWithoutAuthentication()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/health"))
                                .andExpect(status().isOk());
        }

        @Test
        void customerShouldBeAbleToCreateTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                String token = jwtService.generateToken(
                                customer.getEmail());

                String requestBody = """
                                {
                                    "title": "Payment issue",
                                    "description": "My payment failed"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/tickets")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isCreated());
        }

        @Test
        void customerShouldNotAccessAdminUsersEndpoint()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                String token = jwtService.generateToken(
                                customer.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/users")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void agentShouldNotAccessAdminUsersEndpoint()
                        throws Exception {

                User agent = createAgent("PAYMENT_SUPPORT");

                String token = jwtService.generateToken(
                                agent.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/users")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminShouldAccessAdminUsersEndpoint()
                        throws Exception {

                User admin = createUser(Role.ADMIN);

                String token = jwtService.generateToken(
                                admin.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/users")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());
        }

        @Test
        void customerShouldNotAccessAdminDashboard()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                String token = jwtService.generateToken(
                                customer.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/dashboard")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void agentShouldNotAccessAdminDashboard()
                        throws Exception {

                User agent = createAgent("PAYMENT_SUPPORT");

                String token = jwtService.generateToken(
                                agent.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/dashboard")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminShouldAccessAdminDashboard()
                        throws Exception {

                User admin = createUser(Role.ADMIN);

                String token = jwtService.generateToken(
                                admin.getEmail());

                mockMvc.perform(
                                get("/api/v1/admin/dashboard")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());
        }

        @Test
        void invalidJwtShouldReturn401()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/auth/me")
                                                .header(
                                                                "Authorization",
                                                                "Bearer invalid.jwt.token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void customerShouldNotUpdateTicketStatus()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                Ticket ticket = createTestTicket(customer);

                String token = jwtService.generateToken(
                                customer.getEmail());

                String requestBody = """
                                {
                                    "status": "IN_PROGRESS"
                                }
                                """;

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/status",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        void agentShouldBeAbleToUpdateTicketStatus()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);
                User agent = createAgent("PAYMENT_SUPPORT");

                Ticket ticket = createTestTicket(customer);

                String token = jwtService.generateToken(
                                agent.getEmail());

                String requestBody = """
                                {
                                    "status": "IN_PROGRESS"
                                }
                                """;

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/status",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk());
        }

        @Test
        void agentShouldNotAssignTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);
                User agent = createAgent("PAYMENT_SUPPORT");

                Ticket ticket = createTestTicket(customer);

                String token = jwtService.generateToken(
                                agent.getEmail());

                String requestBody = """
                                {
                                    "agentId": %d
                                }
                                """.formatted(agent.getId());

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/assign",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminShouldBeAbleToAssignTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);
                User admin = createUser(Role.ADMIN);
                User agent = createAgent("PAYMENT_SUPPORT");

                Ticket ticket = createTestTicket(customer);

                String token = jwtService.generateToken(
                                admin.getEmail());

                String requestBody = """
                                {
                                    "agentId": %d
                                }
                                """.formatted(agent.getId());

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/assign",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk());
        }

        @Test
        void customerShouldNotAccessAnotherCustomersTicket()
                        throws Exception {

                User customerA = createUser(Role.CUSTOMER);
                User customerB = createUser(Role.CUSTOMER);

                Ticket ticket = createTestTicket(customerB);

                String token = jwtService.generateToken(
                                customerA.getEmail());

                mockMvc.perform(
                                get("/api/v1/tickets/{id}", ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void customerShouldNotUpdateAnotherCustomersTicket()
                        throws Exception {

                User customerA = createUser(Role.CUSTOMER);
                User customerB = createUser(Role.CUSTOMER);

                Ticket ticket = createTestTicket(customerB);

                String token = jwtService.generateToken(
                                customerA.getEmail());

                String requestBody = """
                                {
                                    "status": "IN_PROGRESS"
                                }
                                """;

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/status",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        void paymentAgentShouldNotAccessTechnicalTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                User paymentAgent = createAgent(
                                "PAYMENT_SUPPORT");

                Ticket ticket = createTestTicket(
                                customer,
                                "TECHNICAL",
                                "TECHNICAL_SUPPORT");

                String token = jwtService.generateToken(
                                paymentAgent.getEmail());

                mockMvc.perform(
                                get("/api/v1/tickets/{id}", ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void paymentAgentShouldNotUpdateTechnicalTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);

                User paymentAgent = createAgent(
                                "PAYMENT_SUPPORT");

                Ticket ticket = createTestTicket(
                                customer,
                                "TECHNICAL",
                                "TECHNICAL_SUPPORT");

                String token = jwtService.generateToken(
                                paymentAgent.getEmail());

                String requestBody = """
                                {
                                    "status": "IN_PROGRESS"
                                }
                                """;

                mockMvc.perform(
                                patch(
                                                "/api/v1/tickets/{id}/status",
                                                ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminShouldAccessAnyTicket()
                        throws Exception {

                User customer = createUser(Role.CUSTOMER);
                User admin = createUser(Role.ADMIN);

                Ticket ticket = createTestTicket(customer);

                String token = jwtService.generateToken(
                                admin.getEmail());

                mockMvc.perform(
                                get("/api/v1/tickets/{id}", ticket.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());
        }

        private User createUser(Role role) {

                String email = role.name().toLowerCase()
                                + "-"
                                + UUID.randomUUID()
                                + "@test.com";

                User user = User.builder()
                                .name(role.name() + " Test User")
                                .email(email)
                                .password(
                                                passwordEncoder.encode("Password@123"))
                                .role(role)
                                .authProvider(AuthProvider.LOCAL)
                                .emailVerified(true)
                                .build();

                return userRepository.save(user);
        }

        private User createAgent(String teamName) {

                SupportTeam team = supportTeamRepository
                                .findByName(teamName)
                                .orElseThrow(() -> new RuntimeException(
                                                teamName + " team not found"));

                String email = "agent-"
                                + UUID.randomUUID()
                                + "@test.com";

                User agent = User.builder()
                                .name("Test Agent")
                                .email(email)
                                .password(
                                                passwordEncoder.encode("Password@123"))
                                .role(Role.AGENT)
                                .authProvider(AuthProvider.LOCAL)
                                .emailVerified(true)
                                .supportTeam(team)
                                .build();

                return userRepository.save(agent);
        }

        private Ticket createTestTicket(User customer) {

                return createTestTicket(
                                customer,
                                "PAYMENT",
                                "PAYMENT_SUPPORT");
        }

        private Ticket createTestTicket(
                        User customer,
                        String categoryName,
                        String teamName) {

                SupportTeam team = supportTeamRepository
                                .findByName(teamName)
                                .orElseThrow(() -> new RuntimeException(
                                                teamName + " team not found"));

                TicketCategory category = TicketCategory.valueOf(categoryName);

                Ticket ticket = Ticket.builder()
                                .title("Test Ticket")
                                .description(
                                                "Test ticket for security integration testing")
                                .category(category)
                                .priority(TicketPriority.MEDIUM)
                                .status(TicketStatus.OPEN)
                                .customer(customer)
                                .supportTeam(team)
                                .aiConfidence(1.0)
                                .aiClassificationStatus("COMPLETED")
                                .slaDeadline(
                                                java.time.LocalDateTime.now()
                                                                .plusHours(12))
                                .build();

                return ticketRepository.save(ticket);
        }
}