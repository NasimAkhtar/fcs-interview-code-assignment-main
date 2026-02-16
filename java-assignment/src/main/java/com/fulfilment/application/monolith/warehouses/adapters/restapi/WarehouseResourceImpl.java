package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.exceptions.*;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject
  private WarehouseRepository warehouseRepository;

  @Inject
  private CreateWarehouseUseCase createWarehouseUseCase;

  @Inject
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Inject
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    LOGGER.debug("Fetching all warehouse units");
    try {
      var result = warehouseRepository
              .getAll()
              .stream()
              .map(this::toWarehouseResponse)
              .toList();

      LOGGER.infof("Successfully fetched %d warehouse units", result.size());
      return result;

    } catch (Exception ex) {
      LOGGER.error("Error while listing warehouse units", ex);
      throw ex;
    }
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infof("Creating new warehouse with businessUnitCode=%s",
            data.getBusinessUnitCode());

    try {
      createWarehouseUseCase.create(fromWarehouse(data));
      LOGGER.info("Warehouse created successfully");
      return data;

    } catch (Exception ex) {
      LOGGER.error("Error while creating warehouse", ex);
      throw ex;
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.debugf("Fetching warehouse by id=%s", id);

    var existing = warehouseRepository.findByBusinessUnitCode(id);

    if (existing == null) {
      LOGGER.warnf("Warehouse not found for id=%s", id);
      throw new WarehouseNotFoundException("Warehouse not found: " + id);
    }

    LOGGER.infof("Warehouse found for id=%s", id);
    return toWarehouseResponse(existing);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.infof("Archiving warehouse id=%s", id);

    var warehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (warehouse == null) {
      LOGGER.warnf("Attempted to archive non-existing warehouse id=%s", id);
      throw new WarehouseNotFoundException("Warehouse not found: " + id);
    }

    archiveWarehouseUseCase.archive(warehouse);
    LOGGER.infof("Warehouse archived successfully id=%s", id);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode,
          @NotNull Warehouse data) {

    LOGGER.infof("Replacing warehouse for businessUnitCode=%s",
            businessUnitCode);

    try {
      replaceWarehouseUseCase.replace(fromWarehouse(data));
      LOGGER.info("Warehouse replaced successfully");
      return data;

    } catch (Exception ex) {
      LOGGER.error("Error while replacing warehouse", ex);
      throw ex;
    }
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
    return mapper.convertValue(data, com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
  }
}
