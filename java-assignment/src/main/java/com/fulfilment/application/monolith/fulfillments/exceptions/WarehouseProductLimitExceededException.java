package com.fulfilment.application.monolith.fulfillments.exceptions;

public class WarehouseProductLimitExceededException extends RuntimeException {
    public WarehouseProductLimitExceededException(String message) {
        super(message);
    }
}
