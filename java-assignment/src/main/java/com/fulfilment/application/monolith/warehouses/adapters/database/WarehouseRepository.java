package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = DbWarehouse.from(warehouse);
    persist(entity);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse existing = find("businessUnitCode", warehouse.getBusinessUnitCode()).firstResult();
    if (existing == null) {
      throw new NotFoundException();
    }
    // Update fields
    existing.updateFrom(warehouse);
    // No need to call persist() — entity is managed
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    delete("businessUnitCode", warehouse.getBusinessUnitCode());
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse existing = find("businessUnitCode", buCode).firstResult();
    return existing.toWarehouse();
  }
}
