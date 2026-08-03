-- Forces default-seeded accounts to set a personal password on first login.
-- Existing production users are not forced (column defaults to FALSE).
ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
