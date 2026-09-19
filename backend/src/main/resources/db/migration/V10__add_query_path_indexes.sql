CREATE INDEX idx_products_public_newest
    ON products(status, created_at DESC, id DESC);
CREATE INDEX idx_products_public_category_newest
    ON products(status, category_id, created_at DESC, id DESC);
CREATE INDEX idx_products_public_vendor_newest
    ON products(status, vendor_id, created_at DESC, id DESC);
CREATE INDEX idx_products_public_price
    ON products(status, price, id);
CREATE INDEX idx_orders_customer_newest
    ON orders(customer_id, created_at DESC, id DESC);
CREATE INDEX idx_orders_status_newest
    ON orders(status, created_at DESC, id DESC);
CREATE INDEX idx_order_items_vendor_order
    ON order_items(vendor_id, order_id);
CREATE INDEX idx_reviews_product_newest
    ON reviews(product_id, created_at DESC, id DESC);
CREATE INDEX idx_users_role_status_newest
    ON users(role, status, created_at DESC, id DESC);