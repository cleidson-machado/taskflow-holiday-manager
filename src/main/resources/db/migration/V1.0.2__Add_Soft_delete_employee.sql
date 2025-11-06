-- ============================================================================
-- Migration V1.0.2 - Add Soft Delete Audit Fields
-- Description: Adds deleted_at and deleted_by fields for soft delete audit
-- Author: System Generated
-- Date: 2025-11-06
-- ============================================================================

-- Add soft delete audit fields to employee_record
ALTER TABLE employee_record
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

-- Create index for deleted_at (useful for queries filtering deleted records)
CREATE INDEX idx_employee_deleted_at ON employee_record(deleted_at);

-- Add comments
COMMENT ON COLUMN employee_record.deleted_at IS 'Timestamp when the employee was soft-deleted';
COMMENT ON COLUMN employee_record.deleted_by IS 'User who performed the soft delete';

-- ============================================================================
-- Apply same changes to other tables with soft delete
-- ============================================================================

-- user_record
ALTER TABLE user_record
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

CREATE INDEX idx_user_deleted_at ON user_record(deleted_at);

COMMENT ON COLUMN user_record.deleted_at IS 'Timestamp when the user was soft-deleted';
COMMENT ON COLUMN user_record.deleted_by IS 'User who performed the soft delete';

-- vacation_request
ALTER TABLE vacation_request
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

CREATE INDEX idx_vacation_request_deleted_at ON vacation_request(deleted_at);

COMMENT ON COLUMN vacation_request.deleted_at IS 'Timestamp when the vacation request was soft-deleted';
COMMENT ON COLUMN vacation_request.deleted_by IS 'User who performed the soft delete';

-- vacation_reservation
ALTER TABLE vacation_reservation
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

CREATE INDEX idx_vacation_reservation_deleted_at ON vacation_reservation(deleted_at);

COMMENT ON COLUMN vacation_reservation.deleted_at IS 'Timestamp when the vacation reservation was soft-deleted';
COMMENT ON COLUMN vacation_reservation.deleted_by IS 'User who performed the soft delete';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================