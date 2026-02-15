package com.fulfilment.application.monolith.warehouses.exceptions;

public class WarehouseCapacityExceededException extends RuntimeException {
    public WarehouseCapacityExceededException(String message) {
        super(message);
    }
}
