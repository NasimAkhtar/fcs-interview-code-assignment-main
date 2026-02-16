package com.fulfilment.application.monolith.fulfillments.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfilmentAssignmentRepository
        implements PanacheRepository<DbFulfilmentAssignment> {
}
