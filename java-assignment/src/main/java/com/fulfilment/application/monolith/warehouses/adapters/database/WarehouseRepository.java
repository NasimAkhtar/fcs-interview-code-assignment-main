package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class.getName());

  @Override
  public List<Warehouse> getAll() {
    LOGGER.debug("Fetching all active warehouses from database");
    var result = this.listAll().stream().filter(dbWarehouse -> dbWarehouse.archivedAt == null).map(DbWarehouse::toWarehouse).toList();
    LOGGER.debugf("Found %d active warehouses", result.size());
    return result;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    LOGGER.infof("Persisting new warehouse: %s", warehouse.getBusinessUnitCode());
    DbWarehouse entity = DbWarehouse.from(warehouse);
    persist(entity);
    warehouse.setId(entity.id);
    LOGGER.debugf("Warehouse %s persisted successfully with id %d", warehouse.getBusinessUnitCode(), entity.id);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    LOGGER.infof("Updating warehouse: %s", warehouse.getBusinessUnitCode());
    DbWarehouse existing = find("businessUnitCode", warehouse.getBusinessUnitCode()).firstResult();
    if (existing == null) {
      LOGGER.warnf("Warehouse not found for update: %s", warehouse.getBusinessUnitCode());
      throw new NotFoundException();
    }
    // Update fields
    existing.updateFrom(warehouse);
    LOGGER.debugf("Warehouse %s updated successfully", warehouse.getBusinessUnitCode());
    // No need to call persist() — entity is managed
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    LOGGER.infof("Removing warehouse: %s", warehouse.getBusinessUnitCode());
    long deleted = delete("businessUnitCode", warehouse.getBusinessUnitCode());
    LOGGER.debugf("Deleted %d records for businessUnitCode: %s", deleted, warehouse.getBusinessUnitCode());
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    LOGGER.debugf("Finding active warehouse by businessUnitCode: %s", buCode);
    DbWarehouse existing = find("businessUnitCode", buCode)
            .stream()
            .filter(dbWarehouse -> dbWarehouse.archivedAt == null)
            .findFirst()
            .orElse(null);

    if (existing == null) {
      LOGGER.debugf("No active warehouse found for businessUnitCode: %s", buCode);
      return null;
    }

    LOGGER.debugf("Active warehouse found for businessUnitCode: %s", buCode);
    return existing.toWarehouse();
  }
}
