package com.aryadeep.backend.specification;

import org.springframework.data.jpa.domain.Specification;

import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import com.aryadeep.backend.entity.User;

public class TicketSpecification {

    public static Specification<Ticket> hasStatus(
            TicketStatus status) {

        return (root, query, criteriaBuilder) ->
                status == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("status"),
                                status
                        );
    }

    public static Specification<Ticket> hasPriority(
            TicketPriority priority) {

        return (root, query, criteriaBuilder) ->
                priority == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("priority"),
                                priority
                        );
    }

    public static Specification<Ticket> hasCategory(
            TicketCategory category) {

        return (root, query, criteriaBuilder) ->
                category == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("category"),
                                category
                        );
    }

    public static Specification<Ticket> hasSlaBreached(
            Boolean slaBreached) {

        return (root, query, criteriaBuilder) ->
                slaBreached == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("slaBreached"),
                                slaBreached
                        );
    }

    public static Specification<Ticket> containsKeyword(
            String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern =
                    "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("title")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("description")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Ticket> accessibleBy(
            User user) {

        return (root, query, criteriaBuilder) -> {

            if (user.getRole() == Role.ADMIN) {
                return null;
            }

            if (user.getRole() == Role.CUSTOMER) {
                return criteriaBuilder.equal(
                        root.get("customer").get("id"),
                        user.getId()
                );
            }

            if (user.getRole() == Role.AGENT) {

                var assignedToAgent =
                        criteriaBuilder.equal(
                                root.get("assignedAgent").get("id"),
                                user.getId()
                        );

                if (user.getSupportTeam() == null) {
                    return assignedToAgent;
                }

                var sameSupportTeam =
                        criteriaBuilder.equal(
                                root.get("supportTeam").get("id"),
                                user.getSupportTeam().getId()
                        );

                return criteriaBuilder.or(
                        assignedToAgent,
                        sameSupportTeam
                );
            }

            return criteriaBuilder.disjunction();
        };
    }
}