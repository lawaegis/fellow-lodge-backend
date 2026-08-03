-- ============================================================================
-- FELLOW LODGE - Migration parity
-- Version 3 - Restores legacy SQLite indexes not carried into V1
-- ============================================================================

-- Legacy: CREATE INDEX idx_guests_phone ON guests(phone);
CREATE INDEX IF NOT EXISTS idx_guests_phone ON guests(phone);
