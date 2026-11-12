package com.quirogaotero.beans.messaging.application;

import com.quirogaotero.beans.messaging.application.port.out.InMemoryOutbox;
import com.quirogaotero.beans.messaging.application.port.out.MessagePublisher;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RelayOutboxServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryOutbox outbox = new InMemoryOutbox();
    private final List<UUID> sent = new ArrayList<>();

    private MessagePublisher publisherThatSucceeds() {
        return message -> sent.add(message.id());
    }
    private MessagePublisher publisherThatFailsOn(UUID messageId) {
        return message -> {
            if (message.id().equals(messageId)) throw new IllegalStateException("broker unavailable");
            sent.add(message.id());
        };
    }

    private RelayOutboxService service(MessagePublisher publisher, int batchSize) {
        return new RelayOutboxService(outbox, publisher, clock, batchSize);
    }

    private OutboxMessage messageFrom(String occurredAt) {
        var message = new OutboxMessage(UUID.randomUUID(), "some.topic", "SomethingHappened",
                "key-1", "{}", Instant.parse(occurredAt));
        outbox.append(message);
        return message;
    }

    @Test
    void publishes_pending_messages_oldest_first_and_marks_them_published() {
        var later   = messageFrom("2026-07-01T09:30:00Z");
        var earlier = messageFrom("2026-07-01T09:00:00Z");

        int relayed = service(publisherThatSucceeds(), 50).relayPending();

        assertEquals(2, relayed);
        assertEquals(List.of(earlier.id(), later.id()), sent);
        assertEquals(clock.instant(), outbox.publishedAt.get(earlier.id()));
        assertEquals(clock.instant(), outbox.publishedAt.get(later.id()));
    }

    @Test
    void does_not_publish_a_message_twice() {
        messageFrom("2026-07-01T09:00:00Z");
        var service = service(publisherThatSucceeds(), 50);

        service.relayPending();
        int secondRun = service.relayPending();

        assertEquals(0, secondRun);
        assertEquals(1, sent.size());
    }

    @Test
    void takes_at_most_one_batch_per_run() {
        messageFrom("2026-07-01T09:00:00Z");
        messageFrom("2026-07-01T09:10:00Z");
        messageFrom("2026-07-01T09:20:00Z");

        int relayed = service(publisherThatSucceeds(), 2).relayPending();

        assertEquals(2, relayed);
        assertEquals(1, outbox.findPending(50).size());
    }

    @Test
    void a_message_the_broker_did_not_acknowledge_stays_pending() {
        var message = messageFrom("2026-07-01T09:00:00Z");
        var service = service(publisherThatFailsOn(message.id()), 50);

        assertThrows(IllegalStateException.class, service::relayPending);

        assertFalse(outbox.publishedAt.containsKey(message.id()));
        assertEquals(message.id(), outbox.findPending(50).getFirst().id());
    }

}
