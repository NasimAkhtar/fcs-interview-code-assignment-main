package com.fulfilment.application.monolith.fulfillments.exceptions;

public class StoreFulfilmentLimitExceededException extends RuntimeException {
    public StoreFulfilmentLimitExceededException(String message) {
        super(message);
    }
}
