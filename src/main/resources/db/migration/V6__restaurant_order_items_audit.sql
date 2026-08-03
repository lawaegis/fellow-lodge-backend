-- ============================================================
-- FELLOW LODGE - V6: restaurant_order_items audit columns
-- RestaurantOrderItem extends AuditableEntity, so the table must
-- carry created_at / updated_at to satisfy Hibernate ddl-auto=validate.
-- ============================================================

ALTER TABLE restaurant_order_items
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE restaurant_order_items
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
