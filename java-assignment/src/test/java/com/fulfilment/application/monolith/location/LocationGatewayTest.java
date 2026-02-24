package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  public void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    String identifier = "ZWOLLE-001";

    // when
    Location location = locationGateway.resolveByIdentifier(identifier);

    // then
    assertNotNull(location);
    assertEquals(identifier, location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    // given
    String identifier = "NON-EXISTENT";

    // when
    Location location = locationGateway.resolveByIdentifier(identifier);

    // then
    assertNull(location);
  }
}
