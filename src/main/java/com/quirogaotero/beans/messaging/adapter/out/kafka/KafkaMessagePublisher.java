package com.quirogaotero.beans.messaging.adapter.out.kafka;

import com.quirogaotero.beans.messaging.application.port.out.MessagePublisher;
import com.quirogaotero.beans.messaging.application.port.out.OutboxMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaMessagePublisher implements MessagePublisher {

    public static final String EVENT_ID = "event-id";
    public static final String EVENT_TYPE = "event-type";

    private static final long ACK_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafka;

    public KafkaMessagePublisher(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    @Override
    public void publish(OutboxMessage message) {
        var record = new ProducerRecord<>(message.topic(), message.key(), message.payload());
        record.headers().add(EVENT_ID, message.id().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(EVENT_TYPE, message.type().getBytes(StandardCharsets.UTF_8));

        // Block until the broker acknowledges: the caller marks the message published right after,
        // so "sent" has to mean "the broker has it", not "it is in a client-side buffer".
        try {
            kafka.send(record).get(ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing outbox message " + message.id(), e);
        } catch (ExecutionException | TimeoutException | KafkaException e) {
            throw new IllegalStateException("Kafka did not acknowledge outbox message " + message.id(), e);
        }
    }

}
