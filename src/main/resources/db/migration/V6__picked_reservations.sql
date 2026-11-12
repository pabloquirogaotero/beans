-- Inventory: a confirmed reservation becomes PICKED once its stock has left for the order.

ALTER TABLE reservations DROP CONSTRAINT check_reservation_status;
ALTER TABLE reservations ADD CONSTRAINT check_reservation_status
  CHECK (status IN ('ACTIVE', 'CONFIRMED', 'RELEASED', 'PICKED'));
