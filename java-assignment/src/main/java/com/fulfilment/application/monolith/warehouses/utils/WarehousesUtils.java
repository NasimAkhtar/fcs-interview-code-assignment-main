package com.fulfilment.application.monolith.warehouses.utils;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.exceptions.*;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehousesUtils {
    public void checkIfActiveWarehouseExists(Warehouse newWarehouse, Warehouse existing) {
        if (existing == null) {
            throw new WarehouseNotFoundException(
                    "Active warehouse not found: " + newWarehouse.getBusinessUnitCode());
        }
    }

    public void checkIfWarehouseHaveCapacity(Warehouse newWarehouse, Warehouse existing) {
        if (newWarehouse.getCapacity() < existing.getStock()) {
            throw new WarehouseCapacityExceededException(
                    "New warehouse capacity (" + newWarehouse.getCapacity()
                            + ") cannot accommodate existing stock ("
                            + existing.getStock() + ")");
        }
    }

    public void checkIfNewWareHouseHaveSameStocks(Warehouse newWarehouse, Warehouse existing) {
        if (!newWarehouse.getStock().equals(existing.getStock())) {
            throw new InvalidWarehouseStockException(
                    "New warehouse stock (" + newWarehouse.getStock()
                            + ") must match existing stock ("
                            + existing.getStock() + ")");
        }
    }

    public void checkForWarehouseCapacity(Warehouse warehouse) {
        if (warehouse.getStock() > warehouse.getCapacity()) {
            throw new InvalidWarehouseStockException(
                    "Stock (" + warehouse.getStock()
                            + ") exceeds warehouse capacity ("
                            + warehouse.getCapacity() + ")");
        }
    }

    public void checkForLocationMaxNumberOfWarehouse(Warehouse warehouse, Location location) {
        if (warehouse.getCapacity() > location.maxCapacity) {
            throw new WarehouseCapacityExceededException(
                    "Warehouse capacity (" + warehouse.getCapacity()
                            + ") exceeds location maximum capacity ("
                            + location.maxCapacity + ")");
        }
    }

    public void checkIfWarehouseCanBeCreatedAtLocation(Warehouse warehouse, long warehousesAtLocation, Location location) {
        if (warehousesAtLocation >= location.maxNumberOfWarehouses) {
            throw new WarehouseCapacityExceededException(
                    "Maximum warehouses reached for location: "
                            + warehouse.getLocation());
        }
    }

    public void checkIfLocationExists(Warehouse warehouse, Location location) {
        if (location == null) {
            throw new LocationNotFoundException(
                    "Location not found: " + warehouse.getLocation());
        }
    }

    public void checkIfWarehouseExists(Warehouse warehouse, Warehouse exists) {
        if (exists != null) {
            throw new WareHouseAlreadyExistException(
                    "Warehouse already exists with code: "
                            + warehouse.getBusinessUnitCode());
        }
    }
}
