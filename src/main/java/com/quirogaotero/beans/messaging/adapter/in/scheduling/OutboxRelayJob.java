package com.quirogaotero.beans.messaging.adapter.in.scheduling;

import com.quirogaotero.beans.messaging.application.RelayOutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelayJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayJob.class);

    private final RelayOutboxService relayOutbox;

    public OutboxRelayJob(RelayOutboxService relayOutbox) {
        this.relayOutbox = relayOutbox;
    }

    @Scheduled(fixedDelayString = "${beans.messaging.outbox.relay-interval:PT2S}")
    public void relay() {
        try {
            relayOutbox.relayPending();
        } catch (RuntimeException e) {
            // Nothing is lost: unpublished messages stay in the outbox and go out on a later run.
            log.warn("Outbox relay failed, will retry: {}", e.getMessage());
        }
    }

}
