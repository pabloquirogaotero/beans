-- Messaging: transactional outbox for events relayed to Kafka.

CREATE TABLE outbox_events (
    id           UUID         PRIMARY KEY,
    topic        VARCHAR(128) NOT NULL,
    type         VARCHAR(64)  NOT NULL,
    message_key  VARCHAR(64)  NOT NULL,
    payload      JSONB        NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ
);

-- Partial index for the relay: only rows still waiting to be published.
CREATE INDEX idx_outbox_events_pending ON outbox_events (occurred_at) WHERE published_at IS NULL;
