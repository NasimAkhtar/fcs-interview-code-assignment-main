package com.fulfilment.application.monolith.fulfillments.domains.usecases;

import com.fulfilment.application.monolith.fulfillments.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.fulfillments.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfillments.exceptions.ProductFulfilmentLimitExceededException;
import com.fulfilment.application.monolith.fulfillments.exceptions.StoreFulfilmentLimitExceededException;
import com.fulfilment.application.monolith.fulfillments.exceptions.WarehouseProductLimitExceededException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class FulfilmentService {

    @Inject
    FulfilmentAssignmentRepository repository;

    @Transactional
    public void assignWarehouseToProductAndStore(
            String productCode,
            String storeCode,
            String warehouseCode) {

        // --------------------------------------------------
        // 1️⃣ Constraint:
        // Product can be fulfilled by max 2 warehouses per store
        // --------------------------------------------------

        long productWarehouseCount =
                repository.count(
                        "productCode = ?1 and storeCode = ?2",
                        productCode,
                        storeCode);

        if (productWarehouseCount >= 2) {
            throw new ProductFulfilmentLimitExceededException(
                    "Product " + productCode +
                    " already fulfilled by 2 warehouses for store " + storeCode);
        }

        // --------------------------------------------------
        // 2️⃣ Constraint:
        // Store can be fulfilled by max 3 warehouses
        // --------------------------------------------------

        long distinctWarehousesForStore =
                repository.find("storeCode", storeCode)
                        .project(String.class)
                        .list()
                        .stream()
                        .distinct()
                        .count();

        if (distinctWarehousesForStore >= 3) {
            throw new StoreFulfilmentLimitExceededException(
                    "Store " + storeCode +
                    " already fulfilled by 3 warehouses");
        }

        // --------------------------------------------------
        // 3️⃣ Constraint:
        // Warehouse can store max 5 product types
        // --------------------------------------------------

        long distinctProductsForWarehouse =
                repository.find("warehouseCode", warehouseCode)
                        .project(String.class)
                        .list()
                        .stream()
                        .distinct()
                        .count();

        if (distinctProductsForWarehouse >= 5) {
            throw new WarehouseProductLimitExceededException(
                    "Warehouse " + warehouseCode +
                    " already stores 5 different product types");
        }

        // --------------------------------------------------
        // Create assignment
        // --------------------------------------------------

        DbFulfilmentAssignment assignment = new DbFulfilmentAssignment();
        assignment.productCode = productCode;
        assignment.storeCode = storeCode;
        assignment.warehouseCode = warehouseCode;
        assignment.createdAt = LocalDateTime.now();

        repository.persist(assignment);
    }
}
