-- Ordering context: checkouts and confirmed orders.

CREATE TABLE checkouts (
    id         UUID        PRIMARY KEY,
    status     VARCHAR(16) NOT NULL,
    lines      JSONB       NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT checkout_status_check CHECK (status IN ('STARTED', 'PAID', 'ABANDONED'))
);

CREATE TABLE orders (
    id          UUID        PRIMARY KEY,
    checkout_id UUID        NOT NULL,
    status      VARCHAR(16) NOT NULL,
    lines       JSONB       NOT NULL,
    placed_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT order_status_check CHECK (status IN ('CONFIRMED', 'FULFILLED', 'CANCELLED'))
);

CREATE INDEX idx_orders_checkout ON orders (checkout_id);
