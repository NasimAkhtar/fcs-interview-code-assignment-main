package com.fulfilment.application.monolith.fulfillments.domains.usecases;

import com.fulfilment.application.monolith.fulfillments.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.fulfillments.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfillments.exceptions.ProductFulfilmentLimitExceededException;
import com.fulfilment.application.monolith.fulfillments.exceptions.StoreFulfilmentLimitExceededException;
import com.fulfilment.application.monolith.fulfillments.exceptions.WarehouseProductLimitExceededException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class FulfilmentServiceTest {

    @Inject
    FulfilmentService service;

    @InjectMock
    FulfilmentAssignmentRepository repository;

    // ------------------------------------------------------
    // ✅ SUCCESS CASE
    // ------------------------------------------------------

    @Test
    void shouldPersistAssignmentWhenAllConstraintsAreSatisfied() {

        when(repository.count(anyString(), (Object[]) any()))
                .thenReturn(1L); // < 2 OK

        PanacheQuery queryStore = mock(PanacheQuery.class);
        when(repository.find(eq("storeCode"), (Object[]) any())).thenReturn(queryStore);
        when(queryStore.project(String.class)).thenReturn(queryStore);
        when(queryStore.list()).thenReturn(List.of("W1", "W2")); // distinct 2 < 3 OK

        PanacheQuery queryWarehouse = mock(PanacheQuery.class);
        when(repository.find(eq("warehouseCode"), (Object[]) any())).thenReturn(queryWarehouse);
        when(queryWarehouse.project(String.class)).thenReturn(queryWarehouse);
        when(queryWarehouse.list()).thenReturn(List.of("P1", "P2")); // distinct 2 < 5 OK

        service.assignWarehouseToProductAndStore("P1", "S1", "W1");

        ArgumentCaptor<DbFulfilmentAssignment> captor =
                ArgumentCaptor.forClass(DbFulfilmentAssignment.class);

        verify(repository).persist(captor.capture());

        DbFulfilmentAssignment saved = captor.getValue();

        assertEquals("P1", saved.productCode);
        assertEquals("S1", saved.storeCode);
        assertEquals("W1", saved.warehouseCode);
        assertNotNull(saved.createdAt);
    }

    // ------------------------------------------------------
    // ❌ PRODUCT LIMIT EXCEEDED
    // ------------------------------------------------------

    @Test
    void shouldThrowProductLimitExceededException() {

        when(repository.count(anyString(), (Object[]) any()))
                .thenReturn(2L); // limit reached

        assertThrows(ProductFulfilmentLimitExceededException.class, () ->
                service.assignWarehouseToProductAndStore("P1", "S1", "W1")
        );

        verify(repository, never()).persist(any(DbFulfilmentAssignment.class));
    }

    // ------------------------------------------------------
    // ❌ STORE LIMIT EXCEEDED
    // ------------------------------------------------------

    @Test
    void shouldThrowStoreLimitExceededException() {

        when(repository.count(anyString(), (Object[]) any()))
                .thenReturn(1L);

        PanacheQuery query = mock(PanacheQuery.class);
        when(repository.find(eq("storeCode"), (Object[]) any())).thenReturn(query);
        when(query.project(String.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of("W1", "W2", "W3")); // 3 distinct

        assertThrows(StoreFulfilmentLimitExceededException.class, () ->
                service.assignWarehouseToProductAndStore("P1", "S1", "W1")
        );

        verify(repository, never()).persist(any(DbFulfilmentAssignment.class));
    }

    // ------------------------------------------------------
    // ❌ WAREHOUSE LIMIT EXCEEDED
    // ------------------------------------------------------

    @Test
    void shouldThrowWarehouseProductLimitExceededException() {

        when(repository.count(anyString(), (Object[]) any()))
                .thenReturn(1L);

        PanacheQuery queryStore = mock(PanacheQuery.class);
        when(repository.find(eq("storeCode"), (Object[]) any())).thenReturn(queryStore);
        when(queryStore.project(String.class)).thenReturn(queryStore);
        when(queryStore.list()).thenReturn(List.of("W1")); // < 3 OK

        PanacheQuery queryWarehouse = mock(PanacheQuery.class);
        when(repository.find(eq("warehouseCode"), (Object[]) any())).thenReturn(queryWarehouse);
        when(queryWarehouse.project(String.class)).thenReturn(queryWarehouse);
        when(queryWarehouse.list()).thenReturn(List.of("P1", "P2", "P3", "P4", "P5")); // 5 distinct

        assertThrows(WarehouseProductLimitExceededException.class, () ->
                service.assignWarehouseToProductAndStore("P1", "S1", "W1")
        );

        verify(repository, never()).persist(any(DbFulfilmentAssignment.class));
    }

    // ------------------------------------------------------
    // 🔍 CONSTRAINT ORDER VALIDATION
    // Product limit must short-circuit others
    // ------------------------------------------------------

    @Test
    void shouldNotCheckOtherConstraintsIfProductLimitFails() {

        when(repository.count(anyString(), (Object[]) any()))
                .thenReturn(2L);

        assertThrows(ProductFulfilmentLimitExceededException.class, () ->
                service.assignWarehouseToProductAndStore("P1", "S1", "W1")
        );

        verify(repository, never()).find(anyString(), (Object[]) any());
        verify(repository, never()).persist(any(DbFulfilmentAssignment.class));
    }
}
