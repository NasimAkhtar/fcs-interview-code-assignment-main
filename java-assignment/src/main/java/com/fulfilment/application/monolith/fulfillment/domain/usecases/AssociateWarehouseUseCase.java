package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.adapters.database.Fulfillment;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AssociateWarehouseUseCase {

    @Inject
    ProductRepository productRepository;

    @Inject
    WarehouseRepository warehouseRepository;

    @Transactional
    public void associate(Long productId, Long storeId, Long warehouseId) {
        // 0. Verify entities exist
        if (productRepository.findById(productId) == null) {
            throw new NotFoundException("Product not found: " + productId);
        }
        if (Store.findById(storeId) == null) {
            throw new NotFoundException("Store not found: " + storeId);
        }
        var warehouse = warehouseRepository.findById(warehouseId);
        if (warehouse == null || warehouse.archivedAt != null) {
            throw new NotFoundException("Active Warehouse not found: " + warehouseId);
        }

        // Check if already associated
        long alreadyAssociated = Fulfillment.count("productId = ?1 and storeId = ?2 and warehouseId = ?3", productId, storeId, warehouseId);
        if (alreadyAssociated > 0) {
            return; // Already done
        }

        // 1. Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
        long warehousesForProductAtStore = Fulfillment.count("productId = ?1 and storeId = ?2", productId, storeId);
        if (warehousesForProductAtStore >= 2) {
            throw new BadRequestException("C1: Product already has maximum of 2 warehouses for this store. Current count: " + warehousesForProductAtStore);
        }

        // 2. Each Store can be fulfilled by a maximum of 3 different Warehouses
        long uniqueWarehousesForStore = Fulfillment.find("storeId = ?1", storeId).list().stream()
                .map(f -> ((Fulfillment)f).warehouseId)
                .distinct()
                .count();
        
        // If the current warehouse is not yet associated with THIS store at all
        long associationOfWarehouseToStore = Fulfillment.count("storeId = ?1 and warehouseId = ?2", storeId, warehouseId);
        if (associationOfWarehouseToStore == 0 && uniqueWarehousesForStore >= 3) {
             throw new BadRequestException("C2: Store already has maximum of 3 different warehouses. Current count: " + uniqueWarehousesForStore);
        }

        // 3. Each Warehouse can store maximally 5 types of Products
        long productTypesInWarehouse = Fulfillment.find("warehouseId = ?1", warehouseId).list().stream()
                .map(f -> ((Fulfillment)f).productId)
                .distinct()
                .count();
        
        // If the current product is not yet associated with THIS warehouse at all
        long associationOfProductToWarehouse = Fulfillment.count("productId = ?1 and warehouseId = ?2", productId, warehouseId);
        if (associationOfProductToWarehouse == 0 && productTypesInWarehouse >= 5) {
            throw new BadRequestException("C3: Warehouse already stores maximum of 5 types of products. Current count: " + productTypesInWarehouse);
        }

        // All constraints passed
        Fulfillment fulfillment = new Fulfillment(productId, storeId, warehouseId);
        fulfillment.persist();
    }
}
