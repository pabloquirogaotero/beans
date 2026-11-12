-- Messaging: events each consumer has already handled, so a redelivery is recognised.

CREATE TABLE processed_events (
    consumer     VARCHAR(64) NOT NULL,
    event_id     UUID        NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (consumer, event_id)
);
