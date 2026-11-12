package com.quirogaotero.beans.messaging.application.port.out;

import java.util.UUID;

/**
 * What each consumer has already handled. Kafka delivers at least once, so a consumer asks
 * here first and skips an event it has seen before.
 */
public interface ProcessedEvents {

    /**
     * Records the event for the consumer and returns true, or returns false if it was already
     * recorded. The record joins the caller's transaction: if handling the event rolls back,
     * so does the record, and the redelivery is handled as new.
     */
    boolean markProcessed(String consumer, UUID eventId);

}
