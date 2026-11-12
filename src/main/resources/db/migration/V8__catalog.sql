-- Catalog context: what is sold and at what price. Inventory and Ordering refer to products by SKU only.

CREATE TABLE products (
  sku            VARCHAR(18)  PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  price_in_cents BIGINT       NOT NULL,
  currency       VARCHAR(3)   NOT NULL,

  CONSTRAINT check_positive_product_price CHECK (price_in_cents > 0)
);

-- Demo data, matching the SKUs stocked by V2.
INSERT INTO products (sku, name, price_in_cents, currency) VALUES
  ('COF-COLHU-WBN-250',  'Colombia Huila, whole bean, 250 g',  1250, 'EUR'),
  ('COF-COLHU-WBN-500',  'Colombia Huila, whole bean, 500 g',  2300, 'EUR'),
  ('COF-COLHU-WBN-1000', 'Colombia Huila, whole bean, 1 kg',   4200, 'EUR');
