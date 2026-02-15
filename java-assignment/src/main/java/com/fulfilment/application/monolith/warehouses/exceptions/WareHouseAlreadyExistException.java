package com.fulfilment.application.monolith.warehouses.exceptions;

public class WareHouseAlreadyExistException extends RuntimeException {
  public WareHouseAlreadyExistException(String message) {
    super(message);
  }
}
