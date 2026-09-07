package com.aryadeep.backend.specification;

import com.aryadeep.backend.entity.Ticket;
import com.aryadeep.backend.entity.TicketCategory;
import com.aryadeep.backend.entity.TicketPriority;
import com.aryadeep.backend.entity.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

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
}