-- Ordering: an order records the total that was charged for it.

ALTER TABLE orders ADD COLUMN total_in_cents BIGINT, ADD COLUMN currency VARCHAR(3);

-- Orders placed before prices existed were charged a client-supplied amount that was never stored.
UPDATE orders SET total_in_cents = 0, currency = 'EUR' WHERE total_in_cents IS NULL;

ALTER TABLE orders ALTER COLUMN total_in_cents SET NOT NULL, ALTER COLUMN currency SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT check_non_negative_order_total CHECK (total_in_cents >= 0);
