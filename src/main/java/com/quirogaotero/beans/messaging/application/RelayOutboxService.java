package com.quirogaotero.beans.messaging.application;

import com.quirogaotero.beans.messaging.application.port.out.MessagePublisher;
import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class RelayOutboxService {

    private final Outbox outbox;
    private final MessagePublisher publisher;
    private final Clock clock;
    private final int batchSize;

    public RelayOutboxService(Outbox outbox, MessagePublisher publisher, Clock clock,
                              @Value("${beans.messaging.outbox.batch-size:50}") int batchSize) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    /**
     * Publishes the pending outbox messages, oldest first, and marks each one published.
     *
     * Delivery is at-least-once: if anything fails after the broker acknowledged a message but
     * before this transaction commits, the mark is rolled back and the message goes out again
     * on the next run. Consumers must therefore deduplicate on the message id.
     */
    @Transactional
    public int relayPending() {
        List<OutboxMessage> pending = outbox.findPending(batchSize);
        for (OutboxMessage message : pending) {
            publisher.publish(message);
            outbox.markPublished(message.id(), clock.instant());
        }
        return pending.size();
    }

}
