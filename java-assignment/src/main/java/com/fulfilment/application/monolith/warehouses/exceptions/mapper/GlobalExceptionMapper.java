package com.fulfilment.application.monolith.warehouses.exceptions.mapper;

import com.fulfilment.application.monolith.warehouses.exceptions.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.exceptions.WareHouseAlreadyExistException;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseNotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {

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

        if (exception instanceof LocationNotFoundException) {
            return buildResponse(
                    Response.Status.BAD_REQUEST,
                    "LOCATION_NOT_FOUND",
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
        LOG.error("Unexpected error occurred", exception);

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
