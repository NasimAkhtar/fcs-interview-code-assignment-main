package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    LOGGER.infof("Starting to archive warehouse: %s", warehouse.getBusinessUnitCode());

    LOGGER.debugf("Setting archivedAt to current time for warehouse: %s", warehouse.getBusinessUnitCode());
    warehouse.setArchivedAt(LocalDateTime.now());

    LOGGER.debugf("Updating warehouse store for archive: %s", warehouse.getBusinessUnitCode());
    warehouseStore.update(warehouse);

    LOGGER.infof("Warehouse archived successfully: %s", warehouse.getBusinessUnitCode());
  }
}
