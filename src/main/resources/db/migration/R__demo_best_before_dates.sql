-- Demo data: keep the seeded lots' best-before dates relative to when the database was created,
-- so the demo still sells coffee long after V2 was written. Repeatable: Flyway re-runs it
-- whenever this file changes. Lot 3 is deliberately inside the 30-day shelf-life gate, so the
-- 500 g bag is listed in the catalog but cannot be bought online.

UPDATE lots SET best_before = CURRENT_DATE + 120 WHERE id = '11111111-1111-1111-1111-111111111111';
UPDATE lots SET best_before = CURRENT_DATE + 150 WHERE id = '22222222-2222-2222-2222-222222222222';
UPDATE lots SET best_before = CURRENT_DATE + 20  WHERE id = '33333333-3333-3333-3333-333333333333';
UPDATE lots SET best_before = CURRENT_DATE + 200 WHERE id = '44444444-4444-4444-4444-444444444444';
