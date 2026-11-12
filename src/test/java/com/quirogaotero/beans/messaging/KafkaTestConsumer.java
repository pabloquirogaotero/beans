package com.quirogaotero.beans.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/** Reads a topic from its beginning with a throwaway consumer group, for assertions in tests. */
public final class KafkaTestConsumer {

    private KafkaTestConsumer() { }

    /** Polls for the whole duration, or until {@code expected} matching records have arrived. */
    public static List<ConsumerRecord<String, String>> read(String bootstrapServers, String topic,
                                                            Predicate<ConsumerRecord<String, String>> matching,
                                                            int expected, Duration atMost) {
        var found = new ArrayList<ConsumerRecord<String, String>>();
        try (var consumer = new KafkaConsumer<String, String>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of(topic));
            Instant deadline = Instant.now().plus(atMost);
            while (found.size() < expected && Instant.now().isBefore(deadline))
                consumer.poll(Duration.ofMillis(200)).forEach(r -> { if (matching.test(r)) found.add(r); });
        }
        return found;
    }

    public static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

}
