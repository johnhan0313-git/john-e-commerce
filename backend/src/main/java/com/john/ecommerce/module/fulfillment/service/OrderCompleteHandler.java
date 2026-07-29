package com.john.ecommerce.module.fulfillment.service;

/**
 * Callback interface to avoid circular dependency between fulfillment (logistics)
 * and downstream modules (e.g. settlement). Implementors handle order-complete events.
 */
public interface OrderCompleteHandler {
    void onOrderComplete(Long orderId);
}
