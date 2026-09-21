package com.aryadeep.backend;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class TicketSearchIntegrationTest {

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
    void customerShouldReceivePaginatedTickets() throws Exception {

        User customer = createCustomer();

        createTicket(
                customer,
                "Payment failed",
                "My payment was declined",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        createTicket(
                customer,
                "Website problem",
                "The website is not loading",
                TicketCategory.TECHNICAL,
                TicketPriority.MEDIUM
        );

        createTicket(
                customer,
                "Delivery delayed",
                "My package has not arrived",
                TicketCategory.DELIVERY,
                TicketPriority.LOW
        );

        String token = jwtService.generateToken(customer.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void customerShouldBeAbleToSearchByKeyword() throws Exception {

        User customer = createCustomer();

        createTicket(
                customer,
                "Payment failed",
                "My card payment was declined",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        createTicket(
                customer,
                "Website broken",
                "The checkout page is not loading",
                TicketCategory.TECHNICAL,
                TicketPriority.MEDIUM
        );

        createTicket(
                customer,
                "Package delayed",
                "My delivery has not arrived",
                TicketCategory.DELIVERY,
                TicketPriority.LOW
        );

        String token = jwtService.generateToken(customer.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets")
                        .param("keyword", "payment")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].title").value("Payment failed"));
    }

    @Test
    void customerShouldBeAbleToFilterByPriority() throws Exception {

        User customer = createCustomer();

        createTicket(
                customer,
                "Critical issue",
                "System is unavailable",
                TicketCategory.TECHNICAL,
                TicketPriority.CRITICAL
        );

        createTicket(
                customer,
                "High priority issue",
                "Payment failed",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        createTicket(
                customer,
                "Low priority issue",
                "Small UI problem",
                TicketCategory.TECHNICAL,
                TicketPriority.LOW
        );

        String token = jwtService.generateToken(customer.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
    }

    @Test
    void customerShouldBeAbleToCombineFilters() throws Exception {

        User customer = createCustomer();

        createTicket(
                customer,
                "Payment failed",
                "My payment was declined",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        createTicket(
                customer,
                "Payment pending",
                "Payment is still processing",
                TicketCategory.PAYMENT,
                TicketPriority.MEDIUM
        );

        createTicket(
                customer,
                "Technical issue",
                "Application crashed",
                TicketCategory.TECHNICAL,
                TicketPriority.HIGH
        );

        String token = jwtService.generateToken(customer.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets")
                        .param("keyword", "payment")
                        .param("priority", "HIGH")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].title").value("Payment failed"));
    }

    @Test
    void customerShouldOnlySeeOwnTickets() throws Exception {

        User customerA = createCustomer();
        User customerB = createCustomer();

        createTicket(
                customerA,
                "Customer A payment",
                "Payment issue for customer A",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        createTicket(
                customerA,
                "Customer A delivery",
                "Delivery issue for customer A",
                TicketCategory.DELIVERY,
                TicketPriority.MEDIUM
        );

        createTicket(
                customerB,
                "Customer B payment",
                "Payment issue for customer B",
                TicketCategory.PAYMENT,
                TicketPriority.HIGH
        );

        String token = jwtService.generateToken(customerA.getEmail());

        mockMvc.perform(
                get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].customerId")
                .value(customerA.getId()))
        .andExpect(jsonPath("$.content[1].customerId")
                .value(customerA.getId()));
    }

    private User createCustomer() {

        String email =
                "customer-"
                        + UUID.randomUUID()
                        + "@test.com";

        User customer = User.builder()
                .name("Search Test Customer")
                .email(email)
                .password(
                        passwordEncoder.encode("Password@123"))
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();

        return userRepository.save(customer);
    }

    private Ticket createTicket(
            User customer,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority) {

        String teamName = switch (category) {
            case ACCOUNT -> "ACCOUNT_SUPPORT";
            case PAYMENT, REFUND -> "PAYMENT_SUPPORT";
            case TECHNICAL -> "TECHNICAL_SUPPORT";
            case DELIVERY -> "DELIVERY_SUPPORT";
            case OTHER -> "GENERAL_SUPPORT";
        };

        SupportTeam team = supportTeamRepository
                .findByName(teamName)
                .orElseThrow(() ->
                        new RuntimeException(
                                teamName + " team not found"));

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .category(category)
                .priority(priority)
                .status(TicketStatus.OPEN)
                .customer(customer)
                .supportTeam(team)
                .aiConfidence(1.0)
                .aiClassificationStatus("COMPLETED")
                .slaDeadline(
                        LocalDateTime.now().plusHours(12))
                .build();

        return ticketRepository.save(ticket);
    }
}