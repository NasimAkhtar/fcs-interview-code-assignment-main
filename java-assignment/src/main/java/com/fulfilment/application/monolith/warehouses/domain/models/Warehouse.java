package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Warehouse {

  // unique identifier
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public String getBusinessUnitCode() {
    return businessUnitCode;
  }

  public void setBusinessUnitCode(String businessUnitCode) {
    this.businessUnitCode = businessUnitCode;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getArchivedAt() {
    return archivedAt;
  }

  public void setArchivedAt(LocalDateTime archivedAt) {
    this.archivedAt = archivedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Warehouse warehouse = (Warehouse) o;
    return Objects.equals(businessUnitCode, warehouse.businessUnitCode) && Objects.equals(location, warehouse.location) && Objects.equals(capacity, warehouse.capacity) && Objects.equals(stock, warehouse.stock) && Objects.equals(createdAt, warehouse.createdAt) && Objects.equals(archivedAt, warehouse.archivedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessUnitCode, location, capacity, stock, createdAt, archivedAt);
  }

  @Override
  public String toString() {
    return "Warehouse{" +
            "businessUnitCode='" + businessUnitCode + '\'' +
            ", location='" + location + '\'' +
            ", capacity=" + capacity +
            ", stock=" + stock +
            ", createdAt=" + createdAt +
            ", archivedAt=" + archivedAt +
            '}';
  }
}
