-- Create PRODUCTS table for the ETL pipeline
-- This table stores product data imported from CSV files

DROP TABLE IF EXISTS PRODUCTS;

CREATE TABLE PRODUCTS (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    import_date TIMESTAMP
);

-- Create index on price for efficient filtering in Step 2
CREATE INDEX idx_products_price ON PRODUCTS(price);

-- Create index on import_date for potential time-based queries
CREATE INDEX idx_products_import_date ON PRODUCTS(import_date);