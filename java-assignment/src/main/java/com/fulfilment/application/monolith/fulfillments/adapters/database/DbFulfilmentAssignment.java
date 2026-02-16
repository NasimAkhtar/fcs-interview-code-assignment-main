package com.fulfilment.application.monolith.fulfillments.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"productCode", "storeCode", "warehouseCode"}
    )
)
public class DbFulfilmentAssignment extends PanacheEntity {

    public String productCode;
    public String storeCode;
    public String warehouseCode;

    public LocalDateTime createdAt;
}
