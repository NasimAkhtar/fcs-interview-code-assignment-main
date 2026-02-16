package com.fulfilment.application.monolith.stores.listeners;

import com.fulfilment.application.monolith.stores.LegacyStoreManagerGateway;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreUpdatedListener {
    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store store) {
        legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
    }
}
