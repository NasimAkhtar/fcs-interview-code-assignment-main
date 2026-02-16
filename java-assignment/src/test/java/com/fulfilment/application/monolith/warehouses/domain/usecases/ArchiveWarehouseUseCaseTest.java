package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.inject.Inject;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ArchiveWarehouseUseCaseTest {

    @Inject
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @InjectMock
    WarehouseStore warehouseStore;

    // ---------------------------------------------------------
    // 1️⃣ Should set archivedAt and update store
    // ---------------------------------------------------------
    @Test
    void shouldArchiveWarehouseAndCallUpdate() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH-001");

        archiveWarehouseUseCase.archive(warehouse);

        ArgumentCaptor<Warehouse> captor =
                ArgumentCaptor.forClass(Warehouse.class);

        verify(warehouseStore, times(1)).update(captor.capture());

        Warehouse updated = captor.getValue();

        assertNotNull(updated.getArchivedAt());
        assertEquals("MWH-001", updated.getBusinessUnitCode());
    }

    // ---------------------------------------------------------
    // 2️⃣ Should set archivedAt to current time (within range)
    // ---------------------------------------------------------
    @Test
    void shouldSetArchivedAtToNow() {

        Warehouse warehouse = new Warehouse();

        LocalDateTime beforeCall = LocalDateTime.now();

        archiveWarehouseUseCase.archive(warehouse);

        LocalDateTime afterCall = LocalDateTime.now();

        assertNotNull(warehouse.getArchivedAt());
        assertTrue(
                !warehouse.getArchivedAt().isBefore(beforeCall) &&
                        !warehouse.getArchivedAt().isAfter(afterCall)
        );

        verify(warehouseStore).update(warehouse);
    }

    // ---------------------------------------------------------
    // 3️⃣ Should throw NullPointerException when warehouse is null
    // ---------------------------------------------------------
    @Test
    void shouldThrowExceptionWhenWarehouseIsNull() {

        assertThrows(NullPointerException.class, () ->
                archiveWarehouseUseCase.archive(null)
        );

        verifyNoInteractions(warehouseStore);
    }
}

