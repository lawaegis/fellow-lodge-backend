-- ============================================================================
-- FELLOW LODGE - Content management modules (V4)
-- Restaurant menu, event packages, conference packages, homepage banners,
-- announcements, hotel policies and FAQs. Every table below is readable by
-- the public guest portal through /api/public/** and managed by staff through
-- the corresponding admin CRUD controllers.
-- ============================================================================

-- ============================================================
-- RESTAURANT MENU
-- ============================================================
CREATE TABLE IF NOT EXISTS menu_categories (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) UNIQUE NOT NULL,
    description VARCHAR(2000),
    image_url   VARCHAR(500),
    sort_order  INTEGER DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS menu_items (
    id           UUID PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    description  VARCHAR(2000),
    price        DECIMAL(12,2) NOT NULL DEFAULT 0,
    category_id  UUID REFERENCES menu_categories(id) ON DELETE SET NULL,
    image_url    VARCHAR(500),
    ingredients  VARCHAR(2000),
    is_available BOOLEAN DEFAULT TRUE,
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- EVENT & CONFERENCE PACKAGES
-- ============================================================
CREATE TABLE IF NOT EXISTS event_packages (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    includes    VARCHAR(2000),
    price       DECIMAL(12,2) DEFAULT 0,
    capacity    INTEGER DEFAULT 0,
    image_url   VARCHAR(500),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conference_packages (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    includes    VARCHAR(2000),
    price       DECIMAL(12,2) DEFAULT 0,
    capacity    INTEGER DEFAULT 0,
    image_url   VARCHAR(500),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- HOMEPAGE BANNERS & ANNOUNCEMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS banners (
    id         UUID PRIMARY KEY,
    title      VARCHAR(150) NOT NULL,
    subtitle   VARCHAR(500),
    image_url  VARCHAR(500),
    link_url   VARCHAR(500),
    position   VARCHAR(30) DEFAULT 'home',
    is_active  BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS announcements (
    id          UUID PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    message     VARCHAR(3000) NOT NULL,
    priority    VARCHAR(20) NOT NULL DEFAULT 'Medium',
    is_active   BOOLEAN DEFAULT TRUE,
    valid_from  TIMESTAMP,
    valid_to    TIMESTAMP,
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- HOTEL POLICIES & FAQS
-- ============================================================
CREATE TABLE IF NOT EXISTS policies (
    id          UUID PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    category    VARCHAR(100) DEFAULT 'General',
    content     VARCHAR(4000) NOT NULL,
    sort_order  INTEGER DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS faqs (
    id          UUID PRIMARY KEY,
    question    VARCHAR(500) NOT NULL,
    answer      VARCHAR(3000) NOT NULL,
    category    VARCHAR(100) DEFAULT 'General',
    sort_order  INTEGER DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_menu_items_category   ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_available  ON menu_items(is_available);
CREATE INDEX IF NOT EXISTS idx_announcements_active  ON announcements(is_active);
CREATE INDEX IF NOT EXISTS idx_policies_active       ON policies(is_active);
CREATE INDEX IF NOT EXISTS idx_faqs_active           ON faqs(is_active);

-- ============================================================
-- PERMISSIONS
-- ============================================================
INSERT INTO permissions (id, code, name, description) VALUES
    (CAST('b4000000-0000-4000-8000-000000000001' AS UUID), 'MENU:READ',               'Read Menu',              'View restaurant menu categories and items'),
    (CAST('b4000000-0000-4000-8000-000000000002' AS UUID), 'MENU:WRITE',              'Write Menu',             'Create and update menu categories and items'),
    (CAST('b4000000-0000-4000-8000-000000000003' AS UUID), 'MENU:DELETE',             'Delete Menu',            'Delete menu categories and items'),
    (CAST('b4000000-0000-4000-8000-000000000004' AS UUID), 'EVENT_PACKAGES:READ',     'Read Event Packages',    'View event packages'),
    (CAST('b4000000-0000-4000-8000-000000000005' AS UUID), 'EVENT_PACKAGES:WRITE',    'Write Event Packages',   'Create and update event packages'),
    (CAST('b4000000-0000-4000-8000-000000000006' AS UUID), 'EVENT_PACKAGES:DELETE',   'Delete Event Packages',  'Delete event packages'),
    (CAST('b4000000-0000-4000-8000-000000000007' AS UUID), 'CONFERENCE_PACKAGES:READ','Read Conference Packages','View conference packages'),
    (CAST('b4000000-0000-4000-8000-000000000008' AS UUID), 'CONFERENCE_PACKAGES:WRITE','Write Conference Packages','Create and update conference packages'),
    (CAST('b4000000-0000-4000-8000-000000000009' AS UUID), 'CONFERENCE_PACKAGES:DELETE','Delete Conference Packages','Delete conference packages'),
    (CAST('b4000000-0000-4000-8000-000000000010' AS UUID), 'BANNERS:READ',            'Read Banners',           'View homepage banners'),
    (CAST('b4000000-0000-4000-8000-000000000011' AS UUID), 'BANNERS:WRITE',           'Write Banners',          'Create and update homepage banners'),
    (CAST('b4000000-0000-4000-8000-000000000012' AS UUID), 'BANNERS:DELETE',          'Delete Banners',         'Delete homepage banners'),
    (CAST('b4000000-0000-4000-8000-000000000013' AS UUID), 'ANNOUNCEMENTS:READ',      'Read Announcements',     'View announcements'),
    (CAST('b4000000-0000-4000-8000-000000000014' AS UUID), 'ANNOUNCEMENTS:WRITE',     'Write Announcements',    'Create and update announcements'),
    (CAST('b4000000-0000-4000-8000-000000000015' AS UUID), 'ANNOUNCEMENTS:DELETE',    'Delete Announcements',   'Delete announcements'),
    (CAST('b4000000-0000-4000-8000-000000000016' AS UUID), 'POLICIES:READ',           'Read Policies',          'View hotel policies'),
    (CAST('b4000000-0000-4000-8000-000000000017' AS UUID), 'POLICIES:WRITE',          'Write Policies',         'Create and update hotel policies'),
    (CAST('b4000000-0000-4000-8000-000000000018' AS UUID), 'POLICIES:DELETE',         'Delete Policies',        'Delete hotel policies'),
    (CAST('b4000000-0000-4000-8000-000000000019' AS UUID), 'FAQS:READ',               'Read FAQs',              'View FAQs'),
    (CAST('b4000000-0000-4000-8000-000000000020' AS UUID), 'FAQS:WRITE',              'Write FAQs',             'Create and update FAQs'),
    (CAST('b4000000-0000-4000-8000-000000000021' AS UUID), 'FAQS:DELETE',             'Delete FAQs',            'Delete FAQs');

-- ============================================================
-- ROLE -> PERMISSION MAPPING
-- ============================================================
-- Admin: every new permission (Admin is also granted future permissions).
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000001' AS UUID), id FROM permissions
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000001' AS UUID)
      AND rp.permission_id = permissions.id
);

-- Receptionist: content read + operational write (menu, announcements).
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000002' AS UUID), id FROM permissions
WHERE code IN ('MENU:READ', 'MENU:WRITE',
               'EVENT_PACKAGES:READ', 'CONFERENCE_PACKAGES:READ',
               'BANNERS:READ', 'ANNOUNCEMENTS:READ', 'ANNOUNCEMENTS:WRITE',
               'POLICIES:READ', 'FAQS:READ')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000002' AS UUID)
      AND rp.permission_id = permissions.id
);

-- Guest (portal users): read access to all public content modules.
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000004' AS UUID), id FROM permissions
WHERE code IN ('MENU:READ', 'EVENT_PACKAGES:READ', 'CONFERENCE_PACKAGES:READ',
               'BANNERS:READ', 'ANNOUNCEMENTS:READ', 'POLICIES:READ', 'FAQS:READ')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000004' AS UUID)
      AND rp.permission_id = permissions.id
);

-- NOTE: No demo content is seeded here. Menu, events, conference packages,
-- banners, announcements, policies and FAQs are added by the staff through
-- the admin CRUD controllers after the system goes live.
