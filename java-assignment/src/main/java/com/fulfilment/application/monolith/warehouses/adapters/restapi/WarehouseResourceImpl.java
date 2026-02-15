package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.products.ProductResource;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.exceptions.*;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject
  LocationResolver locationResolver;

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());


  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // 1️⃣ Business Unit Code Verification
    var exists = warehouseRepository
            .findByBusinessUnitCode(data.getBusinessUnitCode());

    LOGGER.info("Business Unit: " + exists);

    if (exists != null) {
      throw new WareHouseAlreadyExistException(
              "Warehouse already exists with code: "
                      + data.getBusinessUnitCode());
    }

    // 2️⃣ Location Validation
    Location location =
            locationResolver.resolveByIdentifier(data.getLocation());

    if (location == null) {
      throw new LocationNotFoundException(
              "Location not found: " + data.getLocation());
    }

    // 3️⃣ Warehouse Creation Feasibility
    long warehousesAtLocation =
            warehouseRepository.count("location", data.getLocation());

    if (warehousesAtLocation >= location.maxNumberOfWarehouses) {
      throw new WarehouseCapacityExceededException(
              "Maximum warehouses reached for location: "
                      + data.getLocation());
    }

    // 4️⃣ Capacity must not exceed location maximum
    if (data.getCapacity() > location.maxCapacity) {
      throw new WarehouseCapacityExceededException(
              "Warehouse capacity (" + data.getCapacity()
                      + ") exceeds location maximum capacity ("
                      + location.maxCapacity + ")");
    }

    // 5️⃣ Stock must fit within capacity
    if (data.getStock() > data.getCapacity()) {
      throw new InvalidWarehouseStockException(
              "Stock (" + data.getStock()
                      + ") exceeds warehouse capacity ("
                      + data.getCapacity() + ")");
    }

    var warehouse = fromWarehouse(data);
    warehouse.setCreatedAt(LocalDateTime.now());

    warehouseRepository.create(warehouse);

    return toWarehouseResponse(warehouse);
  }


  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var existing =
            warehouseRepository.findByBusinessUnitCode(id);

    if (existing == null) {
      throw new WarehouseNotFoundException(
              "Warehouse not found: " + id);
    }

    return toWarehouseResponse(existing);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse =
            warehouseRepository.findByBusinessUnitCode(id);

    if (warehouse == null) {
      throw new WarehouseNotFoundException(
              "Warehouse not found: " + id);
    }

    if (warehouse.getArchivedAt() != null) {
      throw new WarehouseAlreadyArchivedException(
              "Warehouse already archived: " + id);
    }

    warehouse.setArchivedAt(LocalDateTime.now());

    warehouseRepository.update(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode,
          @NotNull Warehouse data) {

    // 1️⃣ Fetch existing active warehouse
    var existing =
            warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    if (existing == null) {
      throw new WarehouseNotFoundException(
              "Active warehouse not found: " + businessUnitCode);
    }

    if (existing.getArchivedAt() != null) {
      throw new WarehouseAlreadyArchivedException(
              "Cannot replace an archived warehouse: "
                      + businessUnitCode);
    }

    // 2️⃣ Capacity accommodation validation
    if (data.getCapacity() < existing.getStock()) {
      throw new WarehouseCapacityExceededException(
              "New warehouse capacity (" + data.getCapacity()
                      + ") cannot accommodate existing stock ("
                      + existing.getStock() + ")");
    }

    // 3️⃣ Stock matching validation
    if (!data.getStock().equals(existing.getStock())) {
      throw new InvalidWarehouseStockException(
              "New warehouse stock (" + data.getStock()
                      + ") must match existing stock ("
                      + existing.getStock() + ")");
    }

    // 4️⃣ Archive old warehouse
    existing.setArchivedAt(LocalDateTime.now());
    warehouseRepository.update(existing);

    // 5️⃣ Create new warehouse
    var newWarehouse = fromWarehouse(data);
    newWarehouse.setCreatedAt(LocalDateTime.now());
    warehouseRepository.create(newWarehouse);

    return toWarehouseResponse(newWarehouse);
  }


  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
  private static com.fulfilment.application.monolith.warehouses.domain.models.Warehouse fromWarehouse(Warehouse data) {
    ObjectMapper mapper = new ObjectMapper();
    var warehouse =
            mapper.convertValue(data, com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
    return warehouse;
  }
}
