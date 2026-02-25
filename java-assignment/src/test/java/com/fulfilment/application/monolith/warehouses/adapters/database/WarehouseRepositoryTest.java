package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @jakarta.transaction.Transactional
    void setup() {
        warehouseRepository.deleteAll();
    }

    @Test
    @TestTransaction
    void testCreateAndFindByBusinessUnitCode() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-001");
        warehouse.setLocation("LOCATION-1");
        warehouse.setCapacity(100);
        warehouse.setStock(10);

        warehouseRepository.create(warehouse);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("BU-001");
        assertNotNull(found);
        assertEquals("BU-001", found.getBusinessUnitCode());
        assertEquals("LOCATION-1", found.getLocation());
        assertEquals(100, found.getCapacity());
        assertEquals(10, found.getStock());
    }

    @Test
    @TestTransaction
    void testGetAllOnlyReturnsActive() {
        Warehouse active = new Warehouse();
        active.setBusinessUnitCode("ACTIVE-1");
        active.setLocation("LOC-1");
        warehouseRepository.create(active);

        Warehouse archived = new Warehouse();
        archived.setBusinessUnitCode("ARCHIVED-1");
        archived.setLocation("LOC-1");
        archived.setArchivedAt(java.time.LocalDateTime.now());
        warehouseRepository.create(archived);

        List<Warehouse> all = warehouseRepository.getAll();
        assertEquals(1, all.size());
        assertEquals("ACTIVE-1", all.get(0).getBusinessUnitCode());
    }

    @Test
    @TestTransaction
    void testUpdate() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-UPDATE");
        warehouse.setLocation("LOC-1");
        warehouse.setCapacity(100);
        warehouseRepository.create(warehouse);

        Warehouse toUpdate = warehouseRepository.findByBusinessUnitCode("BU-UPDATE");
        toUpdate.setCapacity(200);
        warehouseRepository.update(toUpdate);

        Warehouse updated = warehouseRepository.findByBusinessUnitCode("BU-UPDATE");
        assertEquals(200, updated.getCapacity());
    }

    @Test
    @TestTransaction
    void testUpdateNonExistingThrowsNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("NON-EXISTING");
        
        assertThrows(NotFoundException.class, () -> warehouseRepository.update(warehouse));
    }

    @Test
    @TestTransaction
    void testRemove() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-REMOVE");
        warehouse.setLocation("LOC-1");
        warehouseRepository.create(warehouse);

        assertNotNull(warehouseRepository.findByBusinessUnitCode("BU-REMOVE"));

        warehouseRepository.remove(warehouse);

        assertNull(warehouseRepository.findByBusinessUnitCode("BU-REMOVE"));
    }

    @Test
    @TestTransaction
    void testFindByBusinessUnitCodeReturnsNullForArchived() {
        Warehouse archived = new Warehouse();
        archived.setBusinessUnitCode("BU-ARCHIVED");
        archived.setLocation("LOC-1");
        archived.setArchivedAt(java.time.LocalDateTime.now());
        warehouseRepository.create(archived);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("BU-ARCHIVED");
        assertNull(found);
    }
}
