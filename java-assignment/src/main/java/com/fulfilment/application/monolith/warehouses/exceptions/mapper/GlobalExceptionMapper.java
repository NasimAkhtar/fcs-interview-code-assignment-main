package com.fulfilment.application.monolith.warehouses.exceptions.mapper;

import com.fulfilment.application.monolith.warehouses.exceptions.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(RuntimeException exception) {

        if (exception instanceof WarehouseNotFoundException) {
            return buildResponse(
                    Response.Status.NOT_FOUND,
                    "WAREHOUSE_NOT_FOUND",
                    exception.getMessage()
            );
        }

        if (exception instanceof WareHouseAlreadyExistException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "WAREHOUSE_ALREADY_EXISTS",
                    exception.getMessage()
            );
        }

        if (exception instanceof WarehouseCapacityExceededException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "WAREHOUSE_LOCATION_CAPACITY_EXCEEDED",
                    exception.getMessage()
            );
        }

        if (exception instanceof InvalidWarehouseStockException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "WAREHOUSE_STOCK_CAPACITY_EXCEEDED",
                    exception.getMessage()
            );
        }



        if (exception instanceof LocationNotFoundException) {
            LOGGER.info("LocationNotFoundException");
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "LOCATION_NOT_FOUND",
                    exception.getMessage()
            );
        }

        if (exception instanceof WarehouseAlreadyArchivedException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "WAREHOUSE_ALREADY_ARCHIVED",
                    exception.getMessage()
            );
        }

        if (exception instanceof jakarta.validation.ConstraintViolationException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    exception.getMessage()
            );
        }

        // 🔥 Fallback for unexpected errors
        LOGGER.error("Unexpected error occurred", exception);

        return buildResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }

    private Response buildResponse(Response.Status status,
                                   String code,
                                   String message) {

        ApiError error = new ApiError(
                code,
                message,
                status.getStatusCode(),
                uriInfo.getPath()
        );

        return Response.status(status)
                .entity(error)
                .build();
    }
}
