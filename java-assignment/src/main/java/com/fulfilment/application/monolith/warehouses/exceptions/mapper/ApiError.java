package com.fulfilment.application.monolith.warehouses.exceptions.mapper;

import java.time.LocalDateTime;

public class ApiError {

    public String code;
    public String message;
    public int status;
    public LocalDateTime timestamp;
    public String path;

    public ApiError(String code, String message, int status, String path) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
