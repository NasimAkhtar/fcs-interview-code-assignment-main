package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Cacheable
public class DbWarehouse {

  @Id @GeneratedValue public Long id;

  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public DbWarehouse() {}

  public Warehouse toWarehouse() {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = this.businessUnitCode;
    warehouse.location = this.location;
    warehouse.capacity = this.capacity;
    warehouse.stock = this.stock;
    warehouse.createdAt = this.createdAt;
    warehouse.archivedAt = this.archivedAt;
    return warehouse;
  }

  public static DbWarehouse from(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.getBusinessUnitCode();
    entity.location = warehouse.getLocation();
    entity.capacity = warehouse.getCapacity();
    entity.stock = warehouse.getStock();
    warehouse.createdAt = warehouse.getCreatedAt();
    warehouse.archivedAt = warehouse.getArchivedAt();
    return entity;
  }

  public void updateFrom(Warehouse warehouse) {
    this.businessUnitCode = warehouse.getBusinessUnitCode();
    this.location = warehouse.getLocation();
    this.capacity = warehouse.getCapacity();
    this.stock = warehouse.getStock();
    this.createdAt = warehouse.getCreatedAt();
    this.archivedAt = warehouse.getArchivedAt();
  }

}
