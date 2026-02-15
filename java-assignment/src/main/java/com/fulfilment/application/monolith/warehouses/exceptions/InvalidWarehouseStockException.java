package com.fulfilment.application.monolith.warehouses.exceptions;

public class InvalidWarehouseStockException extends RuntimeException {
    public InvalidWarehouseStockException(String message) {
        super(message);
    }
}
