package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ReplaceWarehouseUseCaseTest {

    @Inject
    ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @InjectMock
    WarehouseStore warehouseStore;

    @InjectMock
    WarehousesUtils warehousesUtils;

    // ---------------------------------------------------------
    // 1️⃣ HAPPY PATH
    // ---------------------------------------------------------
    @Test
    void shouldReplaceWarehouseSuccessfully() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-001");
        newWarehouse.setCapacity(400);
        newWarehouse.setStock(100);

        Warehouse existing = new Warehouse();
        existing.setBusinessUnitCode("MWH-001");
        existing.setCapacity(200);
        existing.setStock(100);

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        replaceWarehouseUseCase.replace(newWarehouse);

        // Verify order: create first, then update
        InOrder inOrder = inOrder(warehouseStore);
        inOrder.verify(warehouseStore).create(newWarehouse);
        inOrder.verify(warehouseStore).update(existing);

        assertNotNull(newWarehouse.getCreatedAt());
        assertNotNull(existing.getArchivedAt());

        verify(warehouseStore, times(1)).findByBusinessUnitCode("MWH-001");
        verify(warehouseStore, times(1)).update(existing);
        verify(warehouseStore, never()).remove(existing);
        verify(warehouseStore, never()).create(existing);
        assertEquals("MWH-001", newWarehouse.getBusinessUnitCode());
        assertEquals(400, newWarehouse.getCapacity());
        assertEquals(100, newWarehouse.getStock());
    }

    // ---------------------------------------------------------
    // 2️⃣ SHOULD THROW IF ACTIVE WAREHOUSE DOES NOT EXIST
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenActiveWarehouseNotFound() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-404");

        when(warehouseStore.findByBusinessUnitCode("MWH-404"))
                .thenReturn(null);

        doThrow(new IllegalStateException("Active warehouse not found"))
                .when(warehousesUtils)
                .checkIfActiveWarehouseExists(newWarehouse, null);

        assertThrows(IllegalStateException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        verify(warehouseStore, never()).create(any());
        verify(warehouseStore, never()).update(any());
    }

    // ---------------------------------------------------------
    // 3️⃣ SHOULD THROW IF CAPACITY VALIDATION FAILS
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenCapacityValidationFails() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-001");

        Warehouse existing = new Warehouse();

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        doThrow(new IllegalArgumentException("Capacity invalid"))
                .when(warehousesUtils)
                .checkIfWarehouseHaveCapacity(newWarehouse, existing);

        assertThrows(IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        verify(warehouseStore, never()).create(any());
        verify(warehouseStore, never()).update(any());
    }

    // ---------------------------------------------------------
    // 4️⃣ SHOULD THROW IF STOCK VALIDATION FAILS
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenStockValidationFails() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-001");

        Warehouse existing = new Warehouse();

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        doThrow(new IllegalArgumentException("Stock mismatch"))
                .when(warehousesUtils)
                .checkIfNewWareHouseHaveSameStocks(newWarehouse, existing);

        assertThrows(IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        verify(warehouseStore, never()).create(any());
        verify(warehouseStore, never()).update(any());
    }

    // ---------------------------------------------------------
    // 5️⃣ TIMESTAMP VALIDATION
    // ---------------------------------------------------------
    @Test
    void shouldSetTimestampsWithinExecutionWindow() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-001");

        Warehouse existing = new Warehouse();

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        LocalDateTime before = LocalDateTime.now();

        replaceWarehouseUseCase.replace(newWarehouse);

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(newWarehouse.getCreatedAt());
        assertNotNull(existing.getArchivedAt());

        assertTrue(
                !newWarehouse.getCreatedAt().isBefore(before) &&
                        !newWarehouse.getCreatedAt().isAfter(after)
        );

        assertTrue(
                !existing.getArchivedAt().isBefore(before) &&
                        !existing.getArchivedAt().isAfter(after)
        );
    }

    // ---------------------------------------------------------
    // 6️⃣ VERIFY VALIDATION METHODS ARE CALLED
    // ---------------------------------------------------------
    @Test
    void shouldCallAllValidationMethods() {

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH-001");

        Warehouse existing = new Warehouse();

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        replaceWarehouseUseCase.replace(newWarehouse);

        verify(warehousesUtils).checkIfActiveWarehouseExists(newWarehouse, existing);
        verify(warehousesUtils).checkIfWarehouseHaveCapacity(newWarehouse, existing);
        verify(warehousesUtils).checkIfNewWareHouseHaveSameStocks(newWarehouse, existing);

        verify(warehouseStore).create(newWarehouse);
        verify(warehouseStore).update(existing);
    }
}

