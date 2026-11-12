package com.quirogaotero.beans.messaging.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaListenerConfig {

    public static final String DEAD_LETTER_SUFFIX = ".DLT";

    // A message whose handling fails is retried a few times, which covers transient failures
    // such as a lost optimistic-lock race. If it still fails it is parked on <topic>.DLT, so one
    // bad message cannot block every message behind it on the partition.
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafka) {
        var deadLetters = new DeadLetterPublishingRecoverer(kafka,
                (record, e) -> new TopicPartition(record.topic() + DEAD_LETTER_SUFFIX, -1));
        return new DefaultErrorHandler(deadLetters, new FixedBackOff(500L, 4));
    }

}
