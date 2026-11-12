package com.quirogaotero.beans.messaging;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.messaging.application.port.out.ProcessedEvents;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProcessedEventsIT {

    @Autowired
    ProcessedEvents processedEvents;
    @Autowired
    TransactionTemplate transaction;

    @Test
    void accepts_an_event_once_per_consumer() {
        UUID eventId = UUID.randomUUID();

        assertTrue(processedEvents.markProcessed("consumer-a", eventId));
        assertFalse(processedEvents.markProcessed("consumer-a", eventId));
        assertTrue(processedEvents.markProcessed("consumer-b", eventId),
                "Another consumer has its own record of the same event");
    }

    @Test
    void an_event_whose_handling_rolled_back_can_be_handled_again() {
        UUID eventId = UUID.randomUUID();

        transaction.executeWithoutResult(status -> {
            assertTrue(processedEvents.markProcessed("consumer-a", eventId));
            status.setRollbackOnly();
        });

        assertTrue(processedEvents.markProcessed("consumer-a", eventId));
    }

}
