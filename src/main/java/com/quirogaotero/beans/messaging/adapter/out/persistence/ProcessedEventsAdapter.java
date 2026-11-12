package com.quirogaotero.beans.messaging.adapter.out.persistence;

import com.quirogaotero.beans.messaging.application.port.out.ProcessedEvents;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProcessedEventsAdapter implements ProcessedEvents {

    private final JdbcTemplate jdbc;

    public ProcessedEventsAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // One atomic statement instead of "check, then insert": two deliveries of the same event
    // racing each other cannot both get true, the primary key decides.
    @Override public boolean markProcessed(String consumer, UUID eventId) {
        int inserted = jdbc.update(
                "INSERT INTO processed_events (consumer, event_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                consumer, eventId);
        return inserted == 1;
    }

}
