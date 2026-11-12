package com.quirogaotero.beans.messaging;

import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.messaging.application.RelayOutboxService;
import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The outbox end to end: a message stored in PostgreSQL shows up on the real Kafka topic,
 * once, and is then marked published.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OutboxRelayIT {

    @Autowired
    Outbox outbox;
    @Autowired
    RelayOutboxService relayOutbox;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    KafkaContainer kafka;

    private final String topic = "outbox-relay-it." + UUID.randomUUID();

    private OutboxMessage storedMessage() {
        var message = new OutboxMessage(UUID.randomUUID(), topic, "SomethingHappened", "key-1",
                "{\"answer\": 42}", Instant.now());
        outbox.append(message);
        return message;
    }

    private List<ConsumerRecord<String, String>> readTopic(int expected, Duration atMost) {
        return KafkaTestConsumer.read(kafka.getBootstrapServers(), topic, r -> true, expected, atMost);
    }

    private Object publishedAt(UUID messageId) {
        return jdbc.queryForMap("SELECT published_at FROM outbox_events WHERE id = ?", messageId).get("published_at");
    }

    @Test
    void a_stored_message_reaches_its_topic_and_is_marked_published() {
        OutboxMessage message = storedMessage();

        List<ConsumerRecord<String, String>> received = readTopic(1, Duration.ofSeconds(20));

        assertEquals(1, received.size(), "The scheduled relay must publish the message");
        ConsumerRecord<String, String> record = received.getFirst();
        assertEquals("key-1", record.key());
        assertTrue(record.value().contains("42"));
        assertEquals(message.id().toString(), KafkaTestConsumer.header(record, "event-id"));
        assertEquals("SomethingHappened", KafkaTestConsumer.header(record, "event-type"));

        await().atMost(Duration.ofSeconds(5)).until(() -> publishedAt(message.id()) != null);
    }

    @Test
    void a_published_message_is_not_sent_again() {
        OutboxMessage message = storedMessage();
        await().atMost(Duration.ofSeconds(20)).until(() -> publishedAt(message.id()) != null);

        relayOutbox.relayPending();
        relayOutbox.relayPending();

        assertEquals(1, readTopic(2, Duration.ofSeconds(3)).size(), "The topic must hold the message exactly once");
    }

}
