package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseEndpointTest {

    private static final String PATH = "/warehouse";

    // ---------------------------------------------------------
    // 1️⃣ LIST INITIAL DATA
    // ---------------------------------------------------------
    @Test
    @Order(1)
    void shouldListInitialWarehouses() {

        given()
        .when()
            .get(PATH)
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(3))
            .body("businessUnitCode", hasItems(
                    "MWH.001",
                    "MWH.012",
                    "MWH.023"
            ));
    }

    // ---------------------------------------------------------
    // 2️⃣ GET BY ID - SUCCESS
    // ---------------------------------------------------------
    @Test
    @Order(2)
    void shouldReturnWarehouseById() {

        given()
        .when()
            .get(PATH + "/MWH.001")
        .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("MWH.001"))
            .body("location", notNullValue())
            .body("capacity", greaterThanOrEqualTo(100))
            .body("stock", greaterThanOrEqualTo(10));
    }

    // ---------------------------------------------------------
    // 3️⃣ GET BY ID - NOT FOUND
    // ---------------------------------------------------------
    @Test
    @Order(3)
    void shouldReturn404WhenWarehouseNotFound() {

        given()
        .when()
            .get(PATH + "/UNKNOWN-ID")
        .then()
            .statusCode(404);
    }

    // ---------------------------------------------------------
    // 4️⃣ CREATE WAREHOUSE
    // ---------------------------------------------------------
    @Test
    @Order(4)
    void shouldCreateWarehouse() {

        String request = """
            {
              "businessUnitCode": "NEW-AMS-001",
              "location": "AMSTERDAM-001",
              "capacity": 100,
              "stock": 10
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(PATH)
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("businessUnitCode", equalTo("NEW-AMS-001"));

        // Verify persistence
        given()
        .when()
            .get(PATH + "/NEW-AMS-001")
        .then()
            .statusCode(200);
    }

    // ---------------------------------------------------------
    // 5️⃣ REPLACE WAREHOUSE
    // ---------------------------------------------------------
    @Test
    @Order(5)
    void shouldReplaceWarehouse() {

        String update = """
            {
              "businessUnitCode": "NEW-AMS-001",
              "location": "AMSTERDAM-001",
              "capacity": 200,
              "stock": 10
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(update)
        .when()
            .post(PATH + "/NEW-AMS-001"+"/replacement")
        .then()
            .statusCode(200)
            .body("location", equalTo("AMSTERDAM-001"))
            .body("capacity", equalTo(200))
            .body("stock", equalTo(10));


        // Verify persistence
        given()
                .when()
                .get(PATH + "/NEW-AMS-001")
                .then()
                .statusCode(200)
                .body("location", equalTo("AMSTERDAM-001"))
                .body("capacity", equalTo(200))
                .body("stock", equalTo(10));

    }

    // ---------------------------------------------------------
    // 6️⃣ ARCHIVE WAREHOUSE
    // ---------------------------------------------------------
    @Test
    @Order(6)
    void shouldArchiveWarehouse() {

        given()
        .when()
            .delete(PATH + "/NEW-AMS-001")
        .then()
            .statusCode(204);

        // Confirm deletion
        given()
        .when()
            .get(PATH + "/NEW-AMS-001")
        .then()
            .statusCode(404);
    }

    // ---------------------------------------------------------
    // 7️⃣ ARCHIVE NON EXISTING
    // ---------------------------------------------------------
    @Test
    @Order(7)
    void shouldReturn404WhenArchivingUnknownWarehouse() {

        given()
        .when()
            .delete(PATH + "/INVALID-999")
        .then()
            .statusCode(404);
    }

    // ---------------------------------------------------------
    // 8️⃣ VALIDATION ERROR
    // ---------------------------------------------------------
    @Test
    @Order(8)
    void shouldReturn400WhenInvalidPayload() {

        String invalid = """
            {
              "location": "EINDHOVEN",
              "capacity": 200
            }
        """;

        given()
            .contentType(ContentType.JSON)
            .body(invalid)
        .when()
            .post(PATH)
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }
}
