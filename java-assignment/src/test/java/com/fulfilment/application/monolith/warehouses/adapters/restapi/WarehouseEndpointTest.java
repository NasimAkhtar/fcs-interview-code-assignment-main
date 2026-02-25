package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseEndpointTest {

    @jakarta.inject.Inject
    com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository warehouseRepository;

    private static final String PATH = "/warehouse";

    // ---------------------------------------------------------
    // 0️⃣ PREPARE DATA IF MISSING
    // ---------------------------------------------------------
    @Test
    @Order(0)
    void ensureInitialData() {
        System.out.println("[DEBUG_LOG] ensureInitialData started - CLEARING DB");
        
        createIfMissing("MWH.101", "AMSTERDAM-002", 10, 1);
        createIfMissing("MWH.112", "AMSTERDAM-001", 10, 1);
        createIfMissing("MWH.123", "TILBURG-001", 10, 1);
    }

    private void createIfMissing(String buCode, String loc, int cap, int stock) {
        System.out.println("[DEBUG_LOG] Creating warehouse: " + buCode);
        String request = String.format("""
            {
              "businessUnitCode": "%s",
              "location": "%s",
              "capacity": %d,
              "stock": %d
            }
        """, buCode, loc, cap, stock);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(PATH);
    }

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
                    "MWH.101",
                    "MWH.112"
            ));
    }

    // ---------------------------------------------------------
    // 2️⃣ GET BY ID - SUCCESS
    // ---------------------------------------------------------
    @Test
    @Order(2)
    void shouldReturnWarehouseById() {

        // Dynamically find an ID since we can't guarantee ID=1
        String id = given()
            .when()
                .get(PATH)
            .then()
                .statusCode(200)
                .extract().path("find { it.businessUnitCode == 'MWH.101' }.id");

        given()
        .when()
            .get(PATH + "/" + id)
        .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("MWH.101"));
    }

    // ---------------------------------------------------------
    // 3️⃣ GET BY ID - NOT FOUND
    // ---------------------------------------------------------
    @Test
    @Order(3)
    void shouldReturn404WhenWarehouseNotFound() {

        given()
        .when()
            .get(PATH + "/999")
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

        var response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(PATH)
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("businessUnitCode", equalTo("NEW-AMS-001"))
            .extract().as(com.warehouse.api.beans.Warehouse.class);

        String id = response.getId();

        // Verify persistence
        given()
        .when()
            .get(PATH + "/" + id)
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


        // Verify persistence (get all and find the new one, since we don't know the new ID easily here without extraction)
        given()
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("find { it.businessUnitCode == 'NEW-AMS-001' && it.capacity == 200 }.location", equalTo("AMSTERDAM-001"));

    }

    // ---------------------------------------------------------
    // 6️⃣ ARCHIVE WAREHOUSE
    // ---------------------------------------------------------
    @Test
    @Order(6)
    void shouldArchiveWarehouse() {

        // Get the ID of the warehouse we want to archive
        String id = given()
            .when()
                .get(PATH)
            .then()
                .statusCode(200)
                .extract().path("find { it.businessUnitCode == 'NEW-AMS-001' }.id");

        given()
        .when()
            .delete(PATH + "/" + id)
        .then()
            .statusCode(204);

        // Confirm deletion
        given()
        .when()
            .get(PATH + "/" + id)
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
            .delete(PATH + "/999")
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
              "businessUnitCode": "INVALID",
              "location": "NON-EXISTENT",
              "capacity": 200,
              "stock": 10
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
