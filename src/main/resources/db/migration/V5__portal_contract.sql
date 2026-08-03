-- ============================================================================
-- FELLOW LODGE - Guest portal contract modules (V5)
-- Attractions, legal documents, restaurant orders, guest preferences and
-- support tickets. Completes the guest portal contract (public hotel info,
-- restaurant ordering, personal preferences, notifications, support, promos
-- and payments) with owner-scoped guest data.
-- ============================================================================

-- ============================================================
-- ATTRACTIONS (public /public/hotel/attractions)
-- ============================================================
CREATE TABLE IF NOT EXISTS attractions (
    id            UUID PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(3000),
    category      VARCHAR(100) DEFAULT 'Sightseeing',
    address       VARCHAR(500),
    distance_km   DECIMAL(8,2) DEFAULT 0,
    image_url     VARCHAR(500),
    sort_order    INTEGER DEFAULT 0,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- LEGAL DOCUMENTS (public /public/hotel/legal/{slug})
-- ============================================================
CREATE TABLE IF NOT EXISTS legal_documents (
    id          UUID PRIMARY KEY,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    title       VARCHAR(200) NOT NULL,
    content     VARCHAR(10000) NOT NULL,
    category    VARCHAR(100) DEFAULT 'Legal',
    version     VARCHAR(30) DEFAULT '1.0',
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- RESTAURANT ORDERS
-- ============================================================
CREATE TABLE IF NOT EXISTS restaurant_orders (
    id               UUID PRIMARY KEY,
    order_number     VARCHAR(30) NOT NULL UNIQUE,
    guest_id         UUID REFERENCES guests(id) ON DELETE SET NULL,
    user_id          UUID REFERENCES users(id) ON DELETE SET NULL,
    order_type       VARCHAR(30) NOT NULL DEFAULT 'DineIn',
    status           VARCHAR(30) NOT NULL DEFAULT 'Placed',
    subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    special_requests VARCHAR(2000),
    guest_name       VARCHAR(200),
    guest_email      VARCHAR(200),
    guest_phone      VARCHAR(50),
    placed_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS restaurant_order_items (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES restaurant_orders(id) ON DELETE CASCADE,
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id),
    item_name       VARCHAR(150) NOT NULL,
    unit_price      DECIMAL(12,2) NOT NULL DEFAULT 0,
    quantity        INTEGER NOT NULL DEFAULT 1,
    line_total      DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes           VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_restaurant_orders_guest ON restaurant_orders(guest_id);
CREATE INDEX IF NOT EXISTS idx_restaurant_orders_user  ON restaurant_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_restaurant_order_items_order ON restaurant_order_items(order_id);

-- ============================================================
-- GUEST PREFERENCES
-- ============================================================
CREATE TABLE IF NOT EXISTS guest_preferences (
    id           UUID PRIMARY KEY,
    guest_id     UUID NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    room_type    VARCHAR(100),
    floor_pref   VARCHAR(100),
    bed_type     VARCHAR(100),
    dietary_pref VARCHAR(500),
    accessibility VARCHAR(500),
    contact_pref VARCHAR(30),
    newsletter   BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_guest_preferences_user UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id           UUID PRIMARY KEY,
    ticket_number VARCHAR(30) NOT NULL UNIQUE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    guest_id     UUID REFERENCES guests(id) ON DELETE SET NULL,
    subject      VARCHAR(200) NOT NULL,
    message      VARCHAR(4000) NOT NULL,
    category     VARCHAR(100) DEFAULT 'General',
    priority     VARCHAR(20) DEFAULT 'Medium',
    status       VARCHAR(20) DEFAULT 'Open',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_support_tickets_user ON support_tickets(user_id);

-- ============================================================
-- PERMISSIONS
-- ============================================================
INSERT INTO permissions (id, code, name, description) VALUES
    (CAST('b5000000-0000-4000-8000-000000000001' AS UUID), 'RESTAURANT_ORDERS:READ',   'Read Restaurant Orders',   'View restaurant orders'),
    (CAST('b5000000-0000-4000-8000-000000000002' AS UUID), 'RESTAURANT_ORDERS:WRITE',  'Write Restaurant Orders',  'Create and update restaurant orders'),
    (CAST('b5000000-0000-4000-8000-000000000003' AS UUID), 'ATTRACTIONS:READ',         'Read Attractions',         'View attractions'),
    (CAST('b5000000-0000-4000-8000-000000000004' AS UUID), 'ATTRACTIONS:WRITE',        'Write Attractions',        'Create and update attractions'),
    (CAST('b5000000-0000-4000-8000-000000000005' AS UUID), 'ATTRACTIONS:DELETE',       'Delete Attractions',       'Delete attractions'),
    (CAST('b5000000-0000-4000-8000-000000000006' AS UUID), 'LEGAL_DOCS:WRITE',         'Write Legal Documents',    'Create and update legal documents'),
    (CAST('b5000000-0000-4000-8000-000000000007' AS UUID), 'SUPPORT:READ',             'Read Support Tickets',     'View support tickets');

-- Admin: every new permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000001' AS UUID), id FROM permissions
WHERE code IN ('RESTAURANT_ORDERS:READ', 'RESTAURANT_ORDERS:WRITE',
               'ATTRACTIONS:READ', 'ATTRACTIONS:WRITE', 'ATTRACTIONS:DELETE',
               'LEGAL_DOCS:WRITE', 'SUPPORT:READ')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000001' AS UUID)
      AND rp.permission_id = permissions.id
);

-- Receptionist: restaurant order handling + attraction reads.
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000002' AS UUID), id FROM permissions
WHERE code IN ('RESTAURANT_ORDERS:READ', 'RESTAURANT_ORDERS:WRITE',
               'ATTRACTIONS:READ', 'SUPPORT:READ')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000002' AS UUID)
      AND rp.permission_id = permissions.id
);

-- Guest (portal users): read attractions, submit restaurant orders, plus the
-- owner-scoped capabilities required by the portal contract (own payments,
-- own review deletion, file uploads for avatars / review photos).
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000004' AS UUID), id FROM permissions
WHERE code IN ('ATTRACTIONS:READ', 'RESTAURANT_ORDERS:READ', 'RESTAURANT_ORDERS:WRITE',
               'PAYMENTS:READ', 'REVIEWS:DELETE', 'FILES:WRITE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000004' AS UUID)
      AND rp.permission_id = permissions.id
);

-- ============================================================
-- NOTE: No demo content is seeded here. Attractions, legal documents and
-- hotel settings are added by the staff through the admin CRUD controllers
-- after the system goes live.
-- ============================================================
