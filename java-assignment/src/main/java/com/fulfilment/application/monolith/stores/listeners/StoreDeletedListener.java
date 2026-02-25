package com.fulfilment.application.monolith.stores.listeners;

import com.fulfilment.application.monolith.stores.LegacyStoreManagerGateway;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreDeletedListener {

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store store) {
        legacyStoreManagerGateway.removeStoreOnLegacySystem(store);
    }
}
