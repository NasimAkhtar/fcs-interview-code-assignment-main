package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.usecases.AssociateWarehouseUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("fulfillment")
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

    @Inject
    AssociateWarehouseUseCase associateWarehouseUseCase;

    @GET
    @Path("count")
    public Long count() {
        return com.fulfilment.application.monolith.fulfillment.adapters.database.Fulfillment.count();
    }

    @DELETE
    @Path("clear")
    @jakarta.transaction.Transactional
    public void clear() {
        System.out.println("[DEBUG_LOG] Clearing fulfillment table");
        com.fulfilment.application.monolith.fulfillment.adapters.database.Fulfillment.deleteAll();
    }

    @POST
    @Path("associate")
    public Response associate(AssociationRequest request) {
        try {
            associateWarehouseUseCase.associate(request.productId, request.storeId, request.warehouseId);
            return Response.status(204).build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    public static class AssociationRequest {
        public Long productId;
        public Long storeId;
        public Long warehouseId;
    }
}
