package com.fulfilment.application.monolith.fulfillment.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfillment")
@Cacheable
public class Fulfillment extends PanacheEntity {

  @jakarta.persistence.Column(name = "product_id")
  public Long productId;
  @jakarta.persistence.Column(name = "store_id")
  public Long storeId;
  @jakarta.persistence.Column(name = "warehouse_id")
  public Long warehouseId;

  public Fulfillment() {}

  public Fulfillment(Long productId, Long storeId, Long warehouseId) {
    this.productId = productId;
    this.storeId = storeId;
    this.warehouseId = warehouseId;
  }
}
