package com.fulfilment.application.monolith.warehouses.exceptions;

public class WarehouseAlreadyArchivedException extends RuntimeException {
    public WarehouseAlreadyArchivedException(String message) {
        super(message);
    }
}
