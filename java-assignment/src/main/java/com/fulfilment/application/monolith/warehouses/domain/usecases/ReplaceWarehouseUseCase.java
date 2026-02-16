package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesUtils;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehousesUtils warehousesUtils;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore,
                                 WarehousesUtils warehousesUtils) {
    this.warehouseStore = warehouseStore;
    this.warehousesUtils = warehousesUtils;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // 1️⃣ Fetch existing active warehouse
    var existing =
            warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());

    // 1 Check if warehouse exist
    warehousesUtils.checkIfActiveWarehouseExists(newWarehouse, existing);

    // 2 Capacity accommodation validation
    warehousesUtils.checkIfWarehouseHaveCapacity(newWarehouse, existing);

    // 3 Stock matching validation
    warehousesUtils.checkIfNewWareHouseHaveSameStocks(newWarehouse, existing);

    // 4 Create new warehouse
    newWarehouse.setCreatedAt(LocalDateTime.now());
    warehouseStore.create(newWarehouse);

    // 5 Archive old warehouse
    existing.setArchivedAt(LocalDateTime.now());
    warehouseStore.update(existing);
  }
}
