package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesUtils;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseRepository warehouseRepository;

  private final LocationResolver locationResolver;

  private final WarehousesUtils warehousesUtils;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore,
                                WarehouseRepository warehouseRepository,
                                LocationResolver locationResolver,
                                WarehousesUtils warehousesUtils) {
    this.warehouseStore = warehouseStore;
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
    this.warehousesUtils = warehousesUtils;
  }

  @Override
  public void create(Warehouse warehouse) {
    // 1️⃣ Business Unit Code Verification
    var exists = warehouseStore
            .findByBusinessUnitCode(warehouse.getBusinessUnitCode());

    // 1 Warehouse can not be created with same business unit id
    warehousesUtils.checkIfWarehouseExists(warehouse, exists);

    // 2️⃣ Location Validation
    Location location =
            locationResolver.resolveByIdentifier(warehouse.getLocation());

    warehousesUtils.checkIfLocationExists(warehouse, location);

    // 3️⃣ Warehouse Creation Feasibility
    long warehousesAtLocation =
            warehouseRepository.count("location", warehouse.getLocation());

    warehousesUtils.checkIfWarehouseCanBeCreatedAtLocation(warehouse, warehousesAtLocation, location);

    // 4️⃣ Capacity must not exceed location maximum
    warehousesUtils.checkForLocationMaxNumberOfWarehouse(warehouse, location);

    // 5️⃣ Stock must fit within capacity
    warehousesUtils.checkForWarehouseCapacity(warehouse);

    // if all went well, create the warehouse
    warehouse.setCreatedAt(LocalDateTime.now());
    warehouseStore.create(warehouse);
  }
}
