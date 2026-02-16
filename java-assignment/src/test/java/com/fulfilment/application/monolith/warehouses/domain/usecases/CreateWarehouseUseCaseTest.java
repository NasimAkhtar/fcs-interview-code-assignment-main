package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.utils.WarehousesUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class CreateWarehouseUseCaseTest {

    @Inject
    CreateWarehouseUseCase createWarehouseUseCase;

    @InjectMock
    WarehouseStore warehouseStore;

    @InjectMock
    WarehouseRepository warehouseRepository;

    @InjectMock
    LocationResolver locationResolver;

    @InjectMock
    WarehousesUtils warehousesUtils;

    // ---------------------------------------------------------
    // 1️⃣ HAPPY PATH
    // ---------------------------------------------------------
    @Test
    void shouldCreateWarehouseSuccessfully() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH-001");
        warehouse.setLocation("AMSTERDAM");
        warehouse.setCapacity(100);
        warehouse.setStock(50);

        Location location = new Location("ZWOLLE-001", 1, 40);

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);

        when(warehouseRepository.count("location", "AMSTERDAM-001"))
                .thenReturn(5L);

        createWarehouseUseCase.create(warehouse);

        ArgumentCaptor<Warehouse> captor =
                ArgumentCaptor.forClass(Warehouse.class);

        verify(warehouseStore).create(captor.capture());

        Warehouse created = captor.getValue();

        assertNotNull(created.getCreatedAt());
        assertEquals("MWH-001", created.getBusinessUnitCode());
    }

    // ---------------------------------------------------------
    // 2️⃣ WAREHOUSE ALREADY EXISTS
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenWarehouseAlreadyExists() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH-001");

        Warehouse existing = new Warehouse();

        when(warehouseStore.findByBusinessUnitCode("MWH-001"))
                .thenReturn(existing);

        doThrow(new IllegalStateException("Warehouse exists"))
                .when(warehousesUtils)
                .checkIfWarehouseExists(warehouse, existing);

        assertThrows(IllegalStateException.class,
                () -> createWarehouseUseCase.create(warehouse));

        verify(warehouseStore, never()).create(any());
    }

    // ---------------------------------------------------------
    // 3️⃣ LOCATION DOES NOT EXIST
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenLocationNotFound() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH-002");
        warehouse.setLocation("UNKNOWN");

        when(warehouseStore.findByBusinessUnitCode(any()))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("UNKNOWN"))
                .thenReturn(null);

        doThrow(new IllegalArgumentException("Location not found"))
                .when(warehousesUtils)
                .checkIfLocationExists(warehouse, null);

        assertThrows(IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        verify(warehouseStore, never()).create(any());
    }

    // ---------------------------------------------------------
    // 4️⃣ CREATION NOT ALLOWED AT LOCATION
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenWarehouseLimitExceededAtLocation() {

        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("AMSTERDAM");

        Location location = new Location("ZWOLLE-001", 1, 40);

        when(warehouseStore.findByBusinessUnitCode(any()))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier(any()))
                .thenReturn(location);

        when(warehouseRepository.count("location", "AMSTERDAM"))
                .thenReturn(10L);

        doThrow(new IllegalStateException("Too many warehouses"))
                .when(warehousesUtils)
                .checkIfWarehouseCanBeCreatedAtLocation(
                        eq(warehouse),
                        eq(10L),
                        eq(location)
                );

        assertThrows(IllegalStateException.class,
                () -> createWarehouseUseCase.create(warehouse));

        verify(warehouseStore, never()).create(any());
    }

    // ---------------------------------------------------------
    // 5️⃣ CAPACITY EXCEEDS LOCATION MAX
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenCapacityExceedsLocationMax() {

        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("AMSTERDAM");

        Location location = new Location("ZWOLLE-001", 1, 40);

        when(warehouseStore.findByBusinessUnitCode(any()))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier(any()))
                .thenReturn(location);

        doThrow(new IllegalArgumentException("Location max capacity exceeded"))
                .when(warehousesUtils)
                .checkForLocationMaxNumberOfWarehouse(warehouse, location);

        assertThrows(IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        verify(warehouseStore, never()).create(any());
    }

    // ---------------------------------------------------------
    // 6️⃣ STOCK EXCEEDS CAPACITY
    // ---------------------------------------------------------
    @Test
    void shouldThrowWhenStockExceedsCapacity() {

        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("AMSTERDAM");

        Location location = new Location("ZWOLLE-001", 1, 40);

        when(warehouseStore.findByBusinessUnitCode(any()))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier(any()))
                .thenReturn(location);

        doThrow(new IllegalArgumentException("Stock exceeds capacity"))
                .when(warehousesUtils)
                .checkForWarehouseCapacity(warehouse);

        assertThrows(IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        verify(warehouseStore, never()).create(any());
    }

    // ---------------------------------------------------------
    // 7️⃣ CREATED AT TIMESTAMP RANGE VALIDATION
    // ---------------------------------------------------------
    @Test
    void shouldSetCreatedAtToCurrentTime() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH-009");
        warehouse.setLocation("AMSTERDAM");

        Location location = new Location("ZWOLLE-001", 1, 40);

        when(warehouseStore.findByBusinessUnitCode(any()))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier(any()))
                .thenReturn(location);

        LocalDateTime before = LocalDateTime.now();

        createWarehouseUseCase.create(warehouse);

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(warehouse.getCreatedAt());
        assertTrue(
                !warehouse.getCreatedAt().isBefore(before) &&
                        !warehouse.getCreatedAt().isAfter(after)
        );
    }
}

