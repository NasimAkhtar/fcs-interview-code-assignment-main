package com.fulfilment.application.monolith.fulfillments.exceptions;

public class ProductFulfilmentLimitExceededException extends RuntimeException {
    public ProductFulfilmentLimitExceededException(String message) {
        super(message);
    }
}