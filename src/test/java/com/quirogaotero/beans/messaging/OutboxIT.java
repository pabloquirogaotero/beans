package com.quirogaotero.beans.messaging;

import com.jayway.jsonpath.JsonPath;
import com.quirogaotero.beans.TestcontainersConfiguration;
import com.quirogaotero.beans.messaging.application.port.out.Outbox;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OutboxIT {

    @Autowired
    Outbox outbox;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    TransactionTemplate transaction;

    private OutboxMessage newMessage() {
        return new OutboxMessage(UUID.randomUUID(), "outbox-it.events", "SomethingHappened", "key-1",
                "{\"answer\": 42}", Instant.parse("2026-07-01T10:00:00Z"));
    }

    @Test
    void stores_the_message_with_its_json_payload() {
        var message = newMessage();
        outbox.append(message);

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT topic, type, message_key, payload::text AS payload FROM outbox_events WHERE id = ?",
                message.id());
        assertEquals("outbox-it.events", row.get("topic"));
        assertEquals("SomethingHappened", row.get("type"));
        assertEquals("key-1", row.get("message_key"));
        assertEquals(42, JsonPath.<Integer>read((String) row.get("payload"), "$.answer"));
    }

    @Test
    void a_message_appended_in_a_transaction_that_rolls_back_is_never_stored() {
        var message = newMessage();

        transaction.executeWithoutResult(status -> {
            outbox.append(message);
            status.setRollbackOnly();
        });

        Integer stored = jdbc.queryForObject(
                "SELECT count(*) FROM outbox_events WHERE id = ?", Integer.class, message.id());
        assertEquals(0, stored);
    }

}
