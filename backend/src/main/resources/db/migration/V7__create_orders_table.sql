CREATE TABLE orders (
 id BIGSERIAL PRIMARY KEY, customer_id BIGINT NOT NULL REFERENCES users(id), order_number VARCHAR(40) NOT NULL UNIQUE,
 status VARCHAR(20) NOT NULL, subtotal NUMERIC(14,2) NOT NULL CHECK(subtotal>=0), delivery_fee NUMERIC(14,2) NOT NULL CHECK(delivery_fee>=0), total NUMERIC(14,2) NOT NULL CHECK(total>=0), payment_method VARCHAR(30) NOT NULL,
 delivery_full_name VARCHAR(150) NOT NULL, delivery_phone VARCHAR(30) NOT NULL, delivery_address_line1 VARCHAR(255) NOT NULL, delivery_address_line2 VARCHAR(255), delivery_city VARCHAR(100) NOT NULL, delivery_district VARCHAR(100) NOT NULL, delivery_postal_code VARCHAR(20),
 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE order_items (
 id BIGSERIAL PRIMARY KEY, order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE, product_id BIGINT NOT NULL, vendor_id BIGINT NOT NULL,
 product_name VARCHAR(200) NOT NULL, unit_price NUMERIC(14,2) NOT NULL CHECK(unit_price>0), quantity INTEGER NOT NULL CHECK(quantity>0), subtotal NUMERIC(14,2) NOT NULL CHECK(subtotal>0), status VARCHAR(20) NOT NULL
);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_vendor_id ON order_items(vendor_id);
