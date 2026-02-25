package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;

  private final WarehousesUtils warehousesUtils;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore,
                                 WarehousesUtils warehousesUtils) {
    this.warehouseStore = warehouseStore;
    this.warehousesUtils = warehousesUtils;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Starting warehouse replacement for businessUnitCode=%s",
            newWarehouse.getBusinessUnitCode());

    // 1️⃣ Fetch existing active warehouse
    LOGGER.debugf("Fetching existing active warehouse for businessUnitCode=%s",
            newWarehouse.getBusinessUnitCode());
    var existing =
            warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());

    // 1 Check if warehouse exist
    LOGGER.debug("Validating that active warehouse exists");
    warehousesUtils.checkIfActiveWarehouseExists(newWarehouse, existing);

    // 2 Capacity accommodation validation
    LOGGER.debug("Validating capacity accommodation");
    warehousesUtils.checkIfWarehouseHaveCapacity(newWarehouse, existing);

    // 3 Stock matching validation
    LOGGER.debug("Validating stock matching");
    warehousesUtils.checkIfNewWareHouseHaveSameStocks(newWarehouse, existing);

    // 4 Create new warehouse
    LOGGER.debugf("Creating new warehouse record for businessUnitCode=%s",
            newWarehouse.getBusinessUnitCode());
    newWarehouse.setCreatedAt(LocalDateTime.now());
    warehouseStore.create(newWarehouse);

    // 5 Archive old warehouse
    LOGGER.debugf("Archiving existing warehouse for businessUnitCode=%s",
            existing.getBusinessUnitCode());
    existing.setArchivedAt(LocalDateTime.now());
    warehouseStore.update(existing);

    LOGGER.infof("Warehouse replacement completed successfully for businessUnitCode=%s",
            newWarehouse.getBusinessUnitCode());
  }
}
