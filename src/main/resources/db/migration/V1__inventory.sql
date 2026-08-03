-- Inventory context.

CREATE TABLE locations (
  id        VARCHAR(10)       PRIMARY KEY,
  type      VARCHAR(9)        NOT NULL,
  latitude  DOUBLE PRECISION  NOT NULL,
  longitude DOUBLE PRECISION  NOT NULL,

  CONSTRAINT check_location_type CHECK (type IN ('STORE', 'WAREHOUSE'))
);

CREATE TABLE lots (
  id          UUID          PRIMARY KEY,
  sku_id      VARCHAR(18)   NOT NULL,
  code        VARCHAR(9)    UNIQUE NOT NULL,
  best_before DATE          NOT NULL
);

CREATE INDEX index_lots_sku_id ON lots (sku_id);

CREATE TABLE lot_stocks (
  lot_id      UUID        REFERENCES lots,
  location_id VARCHAR(10) REFERENCES locations,
  on_hand     INTEGER     NOT NULL,
  reserved    INTEGER     NOT NULL  DEFAULT 0,
  version     BIGINT      NOT NULL  DEFAULT 0,

  PRIMARY KEY (lot_id, location_id),
  CONSTRAINT check_non_negative_lot_stock_quantities CHECK (on_hand >= 0 AND reserved >= 0),
  CONSTRAINT check_reserved_not_bigger_than_on_hand CHECK (reserved <= on_hand)
);

CREATE TABLE reservations (
  id          UUID        PRIMARY KEY,
  checkout_id UUID        NOT NULL,
  lot_id      UUID        NOT NULL,
  location_id VARCHAR(10) NOT NULL,
  quantity    INTEGER     NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  status      VARCHAR(9)  NOT NULL,
  version     BIGINT      NOT NULL  DEFAULT 0,

  FOREIGN KEY (lot_id, location_id) REFERENCES lot_stocks (lot_id, location_id),
  CONSTRAINT check_positive_reservation_quantity CHECK (quantity > 0),
  CONSTRAINT check_reservation_status CHECK (status IN ('ACTIVE', 'CONFIRMED', 'RELEASED'))
);

CREATE INDEX index_reservations_checkout_id ON reservations (checkout_id);

-- Partial index for the expiry sweep.
CREATE INDEX index_reservations_active_expiry ON reservations(expires_at) WHERE status = 'ACTIVE';
