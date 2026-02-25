package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesValidator;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;

  private final WarehouseRepository warehouseRepository;

  private final LocationResolver locationResolver;

  private final WarehousesValidator warehousesValidator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore,
                                WarehouseRepository warehouseRepository,
                                LocationResolver locationResolver,
                                WarehousesValidator warehousesValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
    this.warehousesValidator = warehousesValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Starting creation of warehouse: businessUnitCode=%s, location=%s",
            warehouse.getBusinessUnitCode(), warehouse.getLocation());

    // 1️⃣ Business Unit Code Verification
    LOGGER.debugf("Checking if warehouse already exists for businessUnitCode=%s",
            warehouse.getBusinessUnitCode());
    var exists = warehouseStore
            .findByBusinessUnitCode(warehouse.getBusinessUnitCode());

    // 1 Warehouse can not be created with same business unit id
    warehousesValidator.checkIfWarehouseExists(warehouse, exists);

    // 2️⃣ Location Validation
    LOGGER.debugf("Resolving location: %s", warehouse.getLocation());
    Location location =
            locationResolver.resolveByIdentifier(warehouse.getLocation());

    warehousesValidator.checkIfLocationExists(warehouse, location);

    // 3️⃣ Warehouse Creation Feasibility
    LOGGER.debugf("Checking warehouse creation feasibility at location: %s",
            warehouse.getLocation());
    long warehousesAtLocation =
            warehouseRepository.count("location", warehouse.getLocation());

    warehousesValidator.checkIfWarehouseCanBeCreatedAtLocation(warehouse, warehousesAtLocation, location);

    // 4️⃣ Capacity must not exceed location maximum
    LOGGER.debug("Validating capacity against location maximum");
    warehousesValidator.checkForLocationMaxNumberOfWarehouse(warehouse, location);

    // 5️⃣ Stock must fit within capacity
    LOGGER.debug("Validating stock capacity");
    warehousesValidator.checkForWarehouseCapacity(warehouse);

    // if all went well, create the warehouse
    LOGGER.infof("All validations passed. Creating warehouse: %s",
            warehouse.getBusinessUnitCode());
    warehouse.setCreatedAt(LocalDateTime.now());
    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse created successfully: %s",
            warehouse.getBusinessUnitCode());
  }
}
