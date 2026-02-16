package com.fulfilment.application.monolith.fulfillments.adapters.rest;

import com.fulfilment.application.monolith.fulfillments.domains.models.FulfilmentRequest;
import com.fulfilment.application.monolith.fulfillments.domains.usecases.FulfilmentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fulfilment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class FulfilmentResource {

    @Inject
    FulfilmentService service;

    @POST
    @Transactional
    public Response assign(FulfilmentRequest request) {

        service.assignWarehouseToProductAndStore(
                request.productCode,
                request.storeCode,
                request.warehouseCode
        );

        return Response.status(Response.Status.CREATED).build();
    }
}
