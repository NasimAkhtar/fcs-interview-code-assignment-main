package com.fulfilment.application.monolith.fulfillments.adapters.rest;

import com.fulfilment.application.monolith.fulfillments.domains.usecases.FulfilmentService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@QuarkusTest
class FulfilmentResourceTest {

    @InjectMock
    FulfilmentService service;

    @Test
    void shouldAssignWarehouseSuccessfully() {

        String body = """
            {
              "productCode": "P1",
              "storeCode": "S1",
              "warehouseCode": "W1"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        verify(service).assignWarehouseToProductAndStore(
                "P1", "S1", "W1"
        );
    }

    @Test
    void shouldReturnBadRequestWhenProductLimitExceeded() {

        doThrow(new com.fulfilment.application.monolith.fulfillments.exceptions.ProductFulfilmentLimitExceededException("Product limit exceeded"))
                .when(service)
                .assignWarehouseToProductAndStore(anyString(), anyString(), anyString());

        String body = """
            {
              "productCode": "P1",
              "storeCode": "S1",
              "warehouseCode": "W1"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenStoreLimitExceeded() {

        doThrow(new com.fulfilment.application.monolith.fulfillments.exceptions.StoreFulfilmentLimitExceededException("Store limit exceeded"))
                .when(service)
                .assignWarehouseToProductAndStore(anyString(), anyString(), anyString());

        String body = """
            {
              "productCode": "P1",
              "storeCode": "S1",
              "warehouseCode": "W1"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenWarehouseProductLimitExceeded() {

        doThrow(new com.fulfilment.application.monolith.fulfillments.exceptions.WarehouseProductLimitExceededException("Warehouse product limit exceeded"))
                .when(service)
                .assignWarehouseToProductAndStore(anyString(), anyString(), anyString());

        String body = """
            {
              "productCode": "P1",
              "storeCode": "S1",
              "warehouseCode": "W1"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/fulfilment")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
