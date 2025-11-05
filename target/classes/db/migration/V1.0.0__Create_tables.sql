-- ============================================================================
-- Migration V1.0.0 - Create Tables
-- Description: Creates initial database schema for Holiday Manager application
-- Author: System Generated
-- Date: 2025-11-05
-- ============================================================================

-- ============================================================================
-- TABLE: employee_record
-- Description: Stores employee information with self-referencing hierarchy
-- ============================================================================
CREATE TABLE employee_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Personal Information
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    fiscal_number VARCHAR(20) UNIQUE,
    social_number VARCHAR(20) UNIQUE,
    date_of_birth DATE,

    -- Contract Information
    contract_role VARCHAR(50) NOT NULL,
    employee_role VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    termination_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    salary_base REAL,

    -- Hierarchical Relationship (Self-referencing)
    manager_id UUID,

    -- Vacation Management
    vacation_days_balance BIGINT NOT NULL DEFAULT 0,
    vacation_days_used BIGINT NOT NULL DEFAULT 0,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Foreign Key Constraint (Self-referencing)
    CONSTRAINT fk_employee_manager FOREIGN KEY (manager_id)
        REFERENCES employee_record(id) ON DELETE SET NULL
);

-- Indexes for employee_record
CREATE INDEX idx_employee_fiscal_number ON employee_record(fiscal_number);
CREATE INDEX idx_employee_social_number ON employee_record(social_number);
CREATE INDEX idx_employee_manager_id ON employee_record(manager_id);
CREATE INDEX idx_employee_is_active ON employee_record(is_active);
CREATE INDEX idx_employee_hire_date ON employee_record(hire_date);

-- ============================================================================
-- TABLE: user_record
-- Description: Stores system user accounts with optional employee association
-- ============================================================================
CREATE TABLE user_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Personal Information
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,

    -- Authentication
    passwd VARCHAR(255) NOT NULL,

    -- Employee Relationship (Optional)
    employee_id UUID UNIQUE,

    -- Account Control
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    is_locked BOOLEAN NOT NULL DEFAULT false,

    -- Additional Information
    phone VARCHAR(20),
    profile_picture_url VARCHAR(500),
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Foreign Key Constraint
    CONSTRAINT fk_user_employee FOREIGN KEY (employee_id)
        REFERENCES employee_record(id) ON DELETE SET NULL
);

-- Indexes for user_record
CREATE INDEX idx_user_email ON user_record(email);
CREATE INDEX idx_user_employee_id ON user_record(employee_id);
CREATE INDEX idx_user_is_active ON user_record(is_active);
CREATE INDEX idx_user_is_verified ON user_record(is_verified);
CREATE INDEX idx_user_is_locked ON user_record(is_locked);

-- ============================================================================
-- TABLE: vacation_request
-- Description: Stores vacation requests with approval workflow
-- ============================================================================
CREATE TABLE vacation_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Employee Relationship
    employee_id UUID NOT NULL,

    -- Vacation Period
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,

    -- Request Status
    vacation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Approval Information
    approving_by VARCHAR(255),
    approval_date TIMESTAMP,

    -- Notes
    request_notes TEXT,
    rejection_reason TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Foreign Key Constraint
    CONSTRAINT fk_vacation_employee FOREIGN KEY (employee_id)
        REFERENCES employee_record(id) ON DELETE CASCADE
);

-- Indexes for vacation_request
CREATE INDEX idx_vacation_request_employee_id ON vacation_request(employee_id);
CREATE INDEX idx_vacation_request_status ON vacation_request(vacation_status);
CREATE INDEX idx_vacation_request_is_active ON vacation_request(is_active);
CREATE INDEX idx_vacation_request_start_date ON vacation_request(start_date);
CREATE INDEX idx_vacation_request_end_date ON vacation_request(end_date);
CREATE INDEX idx_vacation_request_date_range ON vacation_request(start_date, end_date);

-- ============================================================================
-- TABLE: vacation_reservation
-- Description: Stores vacation reservations/planning to prevent conflicts
-- ============================================================================
CREATE TABLE vacation_reservation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Employee Relationship
    employee_id UUID NOT NULL,

    -- Reservation Period
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested BIGINT NOT NULL DEFAULT 0,

    -- Reservation Status
    reservation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_simulation BOOLEAN NOT NULL DEFAULT false,

    -- Approval Information
    approving_by VARCHAR(255),
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,

    -- Cancellation Information
    withdrawal_by VARCHAR(255),

    -- Notes
    notes TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Foreign Key Constraint
    CONSTRAINT fk_reservation_employee FOREIGN KEY (employee_id)
        REFERENCES employee_record(id) ON DELETE CASCADE
);

-- Indexes for vacation_reservation
CREATE INDEX idx_vacation_reservation_employee_id ON vacation_reservation(employee_id);
CREATE INDEX idx_vacation_reservation_status ON vacation_reservation(reservation_status);
CREATE INDEX idx_vacation_reservation_is_active ON vacation_reservation(is_active);
CREATE INDEX idx_vacation_reservation_is_simulation ON vacation_reservation(is_simulation);
CREATE INDEX idx_vacation_reservation_start_date ON vacation_reservation(start_date);
CREATE INDEX idx_vacation_reservation_end_date ON vacation_reservation(end_date);
CREATE INDEX idx_vacation_reservation_date_range ON vacation_reservation(start_date, end_date);

-- ============================================================================
-- COMMENTS
-- ============================================================================

-- Table Comments
COMMENT ON TABLE employee_record IS 'Stores employee records with hierarchical manager-subordinate relationships';
COMMENT ON TABLE user_record IS 'Stores system user accounts with optional employee association';
COMMENT ON TABLE vacation_request IS 'Stores vacation requests with approval workflow';
COMMENT ON TABLE vacation_reservation IS 'Stores vacation reservations/planning to prevent scheduling conflicts';

-- Column Comments for employee_record
COMMENT ON COLUMN employee_record.manager_id IS 'Self-referencing foreign key to establish manager-subordinate hierarchy';
COMMENT ON COLUMN employee_record.vacation_days_balance IS 'Available vacation days balance for the employee';
COMMENT ON COLUMN employee_record.vacation_days_used IS 'Total vacation days used by the employee';

-- Column Comments for user_record
COMMENT ON COLUMN user_record.employee_id IS 'Optional one-to-one relationship with employee record';
COMMENT ON COLUMN user_record.passwd IS 'Encrypted password (should be hashed using BCrypt)';
COMMENT ON COLUMN user_record.failed_login_attempts IS 'Counter for failed login attempts (locks account after 5 attempts)';

-- Column Comments for vacation_request
COMMENT ON COLUMN vacation_request.vacation_status IS 'Status: PENDING, APPROVED, REJECTED, CANCELLED';
COMMENT ON COLUMN vacation_request.days_requested IS 'Number of vacation days requested';

-- Column Comments for vacation_reservation
COMMENT ON COLUMN vacation_reservation.reservation_status IS 'Status: PENDING, APPROVED, REJECTED, CANCELLED';
COMMENT ON COLUMN vacation_reservation.is_simulation IS 'Indicates if this is a simulation/planning reservation';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================