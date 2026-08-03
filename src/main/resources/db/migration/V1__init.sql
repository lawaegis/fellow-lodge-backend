-- ============================================================================
-- FELLOW LODGE - Centralized Database Schema (PostgreSQL / H2 compatible)
-- Version 1 - Initial schema
-- ============================================================================

-- ============================================================
-- ROLES & PERMISSIONS (RBAC)
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id          UUID PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_system   BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
    id          UUID PRIMARY KEY,
    code        VARCHAR(100) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================
-- USERS & STAFF
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(255) UNIQUE NOT NULL,
    full_name             VARCHAR(255) NOT NULL,
    username              VARCHAR(100) UNIQUE NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    role_id               UUID REFERENCES roles(id) ON DELETE SET NULL,
    is_active             BOOLEAN DEFAULT TRUE,
    is_locked             BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until          TIMESTAMP,
    avatar_url            VARCHAR(500),
    last_login            TIMESTAMP,
    last_login_ip         VARCHAR(64),
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staff (
    id          UUID PRIMARY KEY,
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(50),
    position    VARCHAR(100),
    department  VARCHAR(100),
    hire_date   DATE,
    salary      DECIMAL(12,2) DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- GUESTS
-- ============================================================
CREATE TABLE IF NOT EXISTS guests (
    id                UUID PRIMARY KEY,
    user_id           UUID REFERENCES users(id) ON DELETE SET NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    email             VARCHAR(255),
    phone             VARCHAR(50),
    id_type           VARCHAR(50),
    id_number         VARCHAR(100),
    nationality       VARCHAR(100),
    date_of_birth     DATE,
    gender            VARCHAR(20),
    address           VARCHAR(255),
    city              VARCHAR(100),
    country           VARCHAR(100),
    emergency_contact VARCHAR(100),
    is_vip            BOOLEAN DEFAULT FALSE,
    profile_photo     VARCHAR(500),
    notes             VARCHAR(2000),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ROOMS
-- ============================================================
CREATE TABLE IF NOT EXISTS room_types (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    base_price  DECIMAL(12,2) NOT NULL DEFAULT 0,
    max_guests  INTEGER DEFAULT 1,
    bed_type    VARCHAR(50),
    size_sqm    DECIMAL(8,2),
    amenities   VARCHAR(1000),
    is_active   BOOLEAN DEFAULT TRUE,
    image_url   VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS amenities (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    icon        VARCHAR(100),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS room_type_amenities (
    room_type_id UUID NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
    amenity_id   UUID NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (room_type_id, amenity_id)
);

CREATE TABLE IF NOT EXISTS rooms (
    id                UUID PRIMARY KEY,
    room_number       VARCHAR(20) UNIQUE NOT NULL,
    room_type_id      UUID REFERENCES room_types(id) ON DELETE SET NULL,
    floor             INTEGER NOT NULL DEFAULT 1,
    status            VARCHAR(30) NOT NULL DEFAULT 'Available'
                      CHECK (status IN ('Available','Occupied','Maintenance','Reserved','Cleaning','OutOfService')),
    price_per_night   DECIMAL(12,2) NOT NULL,
    extra_charges     DECIMAL(12,2) DEFAULT 0,
    description       VARCHAR(2000),
    has_balcony       BOOLEAN DEFAULT FALSE,
    has_view          BOOLEAN DEFAULT FALSE,
    is_smoking        BOOLEAN DEFAULT FALSE,
    is_accessible     BOOLEAN DEFAULT FALSE,
    notes             VARCHAR(2000),
    image_url         VARCHAR(500),
    last_maintained   TIMESTAMP,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room_images (
    id           UUID PRIMARY KEY,
    room_id      UUID REFERENCES rooms(id) ON DELETE CASCADE,
    room_type_id UUID REFERENCES room_types(id) ON DELETE CASCADE,
    url          VARCHAR(500) NOT NULL,
    caption      VARCHAR(255),
    is_primary   BOOLEAN DEFAULT FALSE,
    sort_order   INTEGER DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- RESERVATIONS, BOOKING CART, CHECK IN / OUT
-- ============================================================
CREATE TABLE IF NOT EXISTS reservations (
    id                   UUID PRIMARY KEY,
    guest_id             UUID REFERENCES guests(id) ON DELETE CASCADE,
    room_id              UUID REFERENCES rooms(id) ON DELETE SET NULL,
    room_type_id         UUID REFERENCES room_types(id) ON DELETE SET NULL,
    booked_by            UUID REFERENCES users(id) ON DELETE SET NULL,
    check_in_date        DATE NOT NULL,
    check_out_date       DATE NOT NULL,
    actual_check_in      TIMESTAMP,
    actual_check_out     TIMESTAMP,
    number_of_guests     INTEGER DEFAULT 1,
    status               VARCHAR(30) NOT NULL DEFAULT 'Pending'
                         CHECK (status IN ('Pending','Confirmed','CheckedIn','CheckedOut','Cancelled','NoShow')),
    total_amount         DECIMAL(12,2),
    discount_percent     DECIMAL(5,2) DEFAULT 0,
    special_requests     VARCHAR(2000),
    cancellation_reason  VARCHAR(1000),
    source               VARCHAR(20) DEFAULT 'DESKTOP',
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS booking_cart (
    id          UUID PRIMARY KEY,
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    guest_id    UUID REFERENCES guests(id) ON DELETE CASCADE,
    session_id  VARCHAR(100),
    status      VARCHAR(30) NOT NULL DEFAULT 'Active'
                CHECK (status IN ('Active','CheckedOut','Abandoned')),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS booking_items (
    id              UUID PRIMARY KEY,
    cart_id         UUID NOT NULL REFERENCES booking_cart(id) ON DELETE CASCADE,
    room_id         UUID REFERENCES rooms(id) ON DELETE SET NULL,
    room_type_id    UUID REFERENCES room_types(id) ON DELETE SET NULL,
    check_in_date   DATE NOT NULL,
    check_out_date  DATE NOT NULL,
    number_of_guests INTEGER DEFAULT 1,
    quantity        INTEGER DEFAULT 1,
    price_per_night DECIMAL(12,2) NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS check_ins (
    id              UUID PRIMARY KEY,
    reservation_id  UUID REFERENCES reservations(id) ON DELETE CASCADE,
    room_id         UUID REFERENCES rooms(id) ON DELETE SET NULL,
    guest_id        UUID REFERENCES guests(id) ON DELETE CASCADE,
    checked_in_by   UUID REFERENCES users(id) ON DELETE SET NULL,
    check_in_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    room_condition  VARCHAR(100) DEFAULT 'Good',
    notes           VARCHAR(2000),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS check_outs (
    id                        UUID PRIMARY KEY,
    check_in_id               UUID REFERENCES check_ins(id) ON DELETE CASCADE,
    checked_out_by            UUID REFERENCES users(id) ON DELETE SET NULL,
    check_out_time            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    room_condition            VARCHAR(100) DEFAULT 'Good',
    mini_bar_charges          DECIMAL(12,2) DEFAULT 0,
    damage_charges            DECIMAL(12,2) DEFAULT 0,
    other_charges             DECIMAL(12,2) DEFAULT 0,
    total_additional_charges  DECIMAL(12,2) DEFAULT 0,
    notes                     VARCHAR(2000),
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- FINANCE: PAYMENTS, INVOICES, TRANSACTIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id          UUID PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id               UUID PRIMARY KEY,
    reservation_id   UUID REFERENCES reservations(id) ON DELETE SET NULL,
    guest_id         UUID REFERENCES guests(id) ON DELETE SET NULL,
    amount           DECIMAL(12,2) NOT NULL,
    payment_method   VARCHAR(50) NOT NULL
                     CHECK (payment_method IN ('Cash','CreditCard','DebitCard','MobileMoney','BankTransfer','Online')),
    payment_status   VARCHAR(30) NOT NULL DEFAULT 'Pending'
                     CHECK (payment_status IN ('Pending','Completed','Failed','Refunded')),
    reference_number VARCHAR(100),
    description      VARCHAR(500),
    received_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    payment_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    id              UUID PRIMARY KEY,
    reservation_id  UUID REFERENCES reservations(id) ON DELETE SET NULL,
    guest_id        UUID REFERENCES guests(id) ON DELETE SET NULL,
    invoice_number  VARCHAR(50) UNIQUE NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'Draft'
                    CHECK (status IN ('Draft','Sent','Paid','Overdue','Cancelled')),
    due_date        DATE,
    issued_by       UUID REFERENCES users(id) ON DELETE SET NULL,
    notes           VARCHAR(2000),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id               UUID PRIMARY KEY,
    invoice_id       UUID REFERENCES invoices(id) ON DELETE SET NULL,
    payment_id       UUID REFERENCES payments(id) ON DELETE SET NULL,
    guest_id         UUID REFERENCES guests(id) ON DELETE SET NULL,
    transaction_type VARCHAR(30) NOT NULL
                     CHECK (transaction_type IN ('PAYMENT','REFUND','ADJUSTMENT')),
    amount           DECIMAL(12,2) NOT NULL,
    status           VARCHAR(30) NOT NULL DEFAULT 'Completed'
                     CHECK (status IN ('Pending','Completed','Failed')),
    reference        VARCHAR(100),
    description      VARCHAR(500),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- HOUSEKEEPING & MAINTENANCE
-- ============================================================
CREATE TABLE IF NOT EXISTS housekeeping (
    id             UUID PRIMARY KEY,
    room_id        UUID REFERENCES rooms(id) ON DELETE SET NULL,
    assigned_to    UUID REFERENCES staff(id) ON DELETE SET NULL,
    task_type      VARCHAR(50) NOT NULL
                   CHECK (task_type IN ('Cleaning','Laundry','Inspection','Restocking','DeepCleaning')),
    status         VARCHAR(30) NOT NULL DEFAULT 'Pending'
                   CHECK (status IN ('Pending','InProgress','Completed','Cancelled')),
    priority       VARCHAR(30) NOT NULL DEFAULT 'Medium'
                   CHECK (priority IN ('Low','Medium','High','Critical')),
    description    VARCHAR(2000),
    notes          VARCHAR(2000),
    scheduled_date DATE,
    completed_date TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS maintenance (
    id             UUID PRIMARY KEY,
    room_id        UUID REFERENCES rooms(id) ON DELETE SET NULL,
    reported_by    UUID REFERENCES users(id) ON DELETE SET NULL,
    assigned_to    UUID REFERENCES staff(id) ON DELETE SET NULL,
    category       VARCHAR(100) NOT NULL,
    description    VARCHAR(2000) NOT NULL,
    priority       VARCHAR(30) NOT NULL DEFAULT 'Medium'
                   CHECK (priority IN ('Low','Medium','High','Critical')),
    status         VARCHAR(30) NOT NULL DEFAULT 'Reported'
                   CHECK (status IN ('Reported','InProgress','Completed','Cancelled')),
    estimated_cost DECIMAL(12,2) DEFAULT 0,
    actual_cost    DECIMAL(12,2),
    reported_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- EVENTS, CONFERENCE HALLS, HOTEL SERVICES
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    event_date  TIMESTAMP,
    location    VARCHAR(255),
    capacity    INTEGER DEFAULT 0,
    price       DECIMAL(12,2) DEFAULT 0,
    image_url   VARCHAR(500),
    status      VARCHAR(30) NOT NULL DEFAULT 'Upcoming'
                CHECK (status IN ('Upcoming','Ongoing','Completed','Cancelled')),
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conference_halls (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    capacity    INTEGER DEFAULT 0,
    hourly_rate DECIMAL(12,2) DEFAULT 0,
    daily_rate  DECIMAL(12,2) DEFAULT 0,
    image_url   VARCHAR(500),
    amenities   VARCHAR(1000),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS event_bookings (
    id                   UUID PRIMARY KEY,
    event_id             UUID REFERENCES events(id) ON DELETE SET NULL,
    conference_hall_id   UUID REFERENCES conference_halls(id) ON DELETE SET NULL,
    guest_id             UUID REFERENCES guests(id) ON DELETE SET NULL,
    booked_by            UUID REFERENCES users(id) ON DELETE SET NULL,
    booking_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_date           TIMESTAMP,
    number_of_attendees  INTEGER DEFAULT 1,
    amount               DECIMAL(12,2) DEFAULT 0,
    status               VARCHAR(30) NOT NULL DEFAULT 'Pending'
                         CHECK (status IN ('Pending','Confirmed','Cancelled','Completed')),
    notes                VARCHAR(2000),
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hotel_services (
    id               UUID PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    description      VARCHAR(2000),
    price            DECIMAL(12,2) DEFAULT 0,
    category         VARCHAR(100),
    duration_minutes INTEGER,
    image_url        VARCHAR(500),
    is_active        BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_bookings (
    id             UUID PRIMARY KEY,
    guest_id       UUID REFERENCES guests(id) ON DELETE SET NULL,
    service_id     UUID REFERENCES hotel_services(id) ON DELETE SET NULL,
    reservation_id UUID REFERENCES reservations(id) ON DELETE SET NULL,
    booking_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    service_time   TIMESTAMP,
    quantity       INTEGER DEFAULT 1,
    total_amount   DECIMAL(12,2) DEFAULT 0,
    status         VARCHAR(30) NOT NULL DEFAULT 'Pending'
                   CHECK (status IN ('Pending','Confirmed','Completed','Cancelled')),
    notes          VARCHAR(2000),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- NOTIFICATIONS, REVIEWS, GALLERY
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
    guest_id   UUID REFERENCES guests(id) ON DELETE CASCADE,
    title      VARCHAR(255) NOT NULL,
    message    VARCHAR(2000) NOT NULL,
    type       VARCHAR(20) NOT NULL DEFAULT 'Info'
               CHECK (type IN ('Info','Warning','Success','Error')),
    is_read    BOOLEAN DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews (
    id             UUID PRIMARY KEY,
    guest_id       UUID REFERENCES guests(id) ON DELETE SET NULL,
    room_id        UUID REFERENCES rooms(id) ON DELETE SET NULL,
    reservation_id UUID REFERENCES reservations(id) ON DELETE SET NULL,
    rating         INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        VARCHAR(2000),
    status         VARCHAR(20) NOT NULL DEFAULT 'Pending'
                   CHECK (status IN ('Pending','Approved','Rejected')),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gallery_images (
    id          UUID PRIMARY KEY,
    title       VARCHAR(255),
    url         VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    category    VARCHAR(100),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- AUDIT, SETTINGS, PROMOTIONS, TOKENS
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users(id) ON DELETE SET NULL,
    username   VARCHAR(100),
    action     VARCHAR(100) NOT NULL,
    table_name VARCHAR(100),
    record_id  VARCHAR(100),
    old_values VARCHAR(4000),
    new_values VARCHAR(4000),
    ip_address VARCHAR(64),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS settings (
    id          UUID PRIMARY KEY,
    "key"       VARCHAR(100) UNIQUE NOT NULL,
    "value"     VARCHAR(2000),
    category    VARCHAR(100) DEFAULT 'general',
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS promotions (
    id               UUID PRIMARY KEY,
    code             VARCHAR(50) UNIQUE NOT NULL,
    name             VARCHAR(150) NOT NULL,
    description      VARCHAR(2000),
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount  DECIMAL(12,2) DEFAULT 0,
    valid_from       DATE,
    valid_to         DATE,
    is_active        BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_users_email      ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username   ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role       ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_staff_user       ON staff(user_id);
CREATE INDEX IF NOT EXISTS idx_guests_email     ON guests(email);
CREATE INDEX IF NOT EXISTS idx_guests_user      ON guests(user_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status     ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_type       ON rooms(room_type_id);
CREATE INDEX IF NOT EXISTS idx_rooms_floor      ON rooms(floor);
CREATE INDEX IF NOT EXISTS idx_reservations_guest  ON reservations(guest_id);
CREATE INDEX IF NOT EXISTS idx_reservations_room   ON reservations(room_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_dates  ON reservations(check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_booking_items_cart  ON booking_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_checkins_reservation  ON check_ins(reservation_id);
CREATE INDEX IF NOT EXISTS idx_checkout_checkin      ON check_outs(check_in_id);
CREATE INDEX IF NOT EXISTS idx_payments_reservation  ON payments(reservation_id);
CREATE INDEX IF NOT EXISTS idx_payments_guest        ON payments(guest_id);
CREATE INDEX IF NOT EXISTS idx_payments_status       ON payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payments_date         ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_invoices_reservation  ON invoices(reservation_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status       ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_number       ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_transactions_invoice  ON transactions(invoice_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_room      ON maintenance(room_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_status    ON maintenance(status);
CREATE INDEX IF NOT EXISTS idx_housekeeping_room     ON housekeeping(room_id);
CREATE INDEX IF NOT EXISTS idx_housekeeping_status   ON housekeeping(status);
CREATE INDEX IF NOT EXISTS idx_housekeeping_assigned ON housekeeping(assigned_to);
CREATE INDEX IF NOT EXISTS idx_notifications_user    ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read    ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_event_bookings_event  ON event_bookings(event_id);
CREATE INDEX IF NOT EXISTS idx_service_bookings_guest ON service_bookings(guest_id);
CREATE INDEX IF NOT EXISTS idx_reviews_guest         ON reviews(guest_id);
CREATE INDEX IF NOT EXISTS idx_audit_user            ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_table           ON audit_logs(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_created         ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user   ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token  ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_user     ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_token    ON password_reset_tokens(token);
