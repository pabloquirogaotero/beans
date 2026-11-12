-- Ordering: a checkout records the total it was priced at when it started.

ALTER TABLE checkouts ADD COLUMN total_in_cents BIGINT, ADD COLUMN currency VARCHAR(3);

-- Checkouts started before prices existed have no total to recover; they are recorded as 0 EUR.
UPDATE checkouts SET total_in_cents = 0, currency = 'EUR' WHERE total_in_cents IS NULL;

ALTER TABLE checkouts ALTER COLUMN total_in_cents SET NOT NULL, ALTER COLUMN currency SET NOT NULL;
ALTER TABLE checkouts ADD CONSTRAINT check_non_negative_checkout_total CHECK (total_in_cents >= 0);
