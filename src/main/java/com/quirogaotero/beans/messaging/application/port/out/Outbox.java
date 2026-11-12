package com.quirogaotero.beans.messaging.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transactional outbox. A message appended here is stored in the same database transaction as
 * the state change that caused it, so the two can never disagree; a relay publishes it later.
 */
public interface Outbox {

    void append(OutboxMessage message);

    /** Oldest first. Entries are locked for the calling transaction, so two relays never pick the same one. */
    List<OutboxMessage> findPending(int limit);

    void markPublished(UUID messageId, Instant publishedAt);

}
