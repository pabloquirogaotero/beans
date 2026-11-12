package com.quirogaotero.beans.messaging.application.port.out;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InMemoryProcessedEvents implements ProcessedEvents {

    private final Set<String> seen = new HashSet<>();

    public boolean markProcessed(String consumer, UUID eventId) { return seen.add(consumer + "/" + eventId); }

}
