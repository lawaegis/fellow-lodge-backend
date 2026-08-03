-- ============================================================================
-- FELLOW LODGE - Admin management endpoints (V7)
-- Grants the permissions needed by the admin support-ticket workflow and the
-- legal-documents module that the desktop application manages. Attractions
-- already have READ/WRITE/DELETE from V5.
-- ============================================================================

INSERT INTO permissions (id, code, name, description) VALUES
    (CAST('b7000000-0000-4000-8000-000000000001' AS UUID), 'SUPPORT:WRITE',    'Write Support Tickets',   'Update support ticket status and priority'),
    (CAST('b7000000-0000-4000-8000-000000000002' AS UUID), 'LEGAL_DOCS:READ',  'Read Legal Documents',    'View legal documents'),
    (CAST('b7000000-0000-4000-8000-000000000003' AS UUID), 'LEGAL_DOCS:DELETE','Delete Legal Documents',  'Delete legal documents');

-- Admin: every new permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT CAST('a1000000-0000-4000-8000-000000000001' AS UUID), id FROM permissions
WHERE code IN ('SUPPORT:WRITE', 'LEGAL_DOCS:READ', 'LEGAL_DOCS:DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = CAST('a1000000-0000-4000-8000-000000000001' AS UUID)
      AND rp.permission_id = permissions.id
);
