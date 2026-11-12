-- Inventory: append-only ledger of every stock movement, for traceability and recalls.

CREATE TABLE stock_movements (
  id          UUID        PRIMARY KEY,
  lot_id      UUID        NOT NULL,
  location_id VARCHAR(10) NOT NULL,
  type        VARCHAR(10) NOT NULL,
  quantity    INTEGER     NOT NULL,
  checkout_id UUID,
  order_id    UUID,
  occurred_at TIMESTAMPTZ NOT NULL,

  FOREIGN KEY (lot_id, location_id) REFERENCES lot_stocks (lot_id, location_id),
  CONSTRAINT check_stock_movement_type CHECK (type IN ('ADJUSTMENT', 'PICK', 'RECEIPT', 'RELEASE', 'RESERVE')),
  CONSTRAINT check_positive_stock_movement_quantity CHECK (quantity > 0),
  CONSTRAINT check_pick_names_its_order CHECK (type <> 'PICK' OR order_id IS NOT NULL)
);

-- A recall starts from the lot.
CREATE INDEX index_stock_movements_lot_id ON stock_movements (lot_id);
