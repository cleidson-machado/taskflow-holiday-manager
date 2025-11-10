-- Parte 1: Criação da Tabela Principal e Restrições Básicas
CREATE TABLE IF NOT EXISTS employee_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,

    fiscal_number VARCHAR(20) UNIQUE,
    fiscal_number_country VARCHAR(2),

    social_number VARCHAR(20) UNIQUE,

    date_of_birth DATE,

    contract_role VARCHAR(50) NOT NULL,
    employee_role VARCHAR(50) NOT NULL,

    hire_date DATE NOT NULL,
    termination_date DATE,

    salary_base REAL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    manager_id UUID, -- Adicionado temporariamente sem FK

    vacation_days_balance BIGINT DEFAULT 0,
    vacation_days_used BIGINT DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Restrições CHECK
    CONSTRAINT chk_hire_date CHECK (hire_date <= CURRENT_DATE),
    CONSTRAINT chk_termination_date CHECK (termination_date IS NULL OR termination_date >= hire_date),
    CONSTRAINT chk_salary_base CHECK (salary_base IS NULL OR salary_base >= 0),
    CONSTRAINT chk_vacation_days_balance CHECK (vacation_days_balance >= 0),
    CONSTRAINT chk_vacation_days_used CHECK (vacation_days_used >= 0)
);

-- Parte 2: Adicionar Chave Estrangeira (Self-Referencing FK deve vir depois da criação da tabela)
-- A sua definição original já estava correta, mas a ordem pode ter causado o problema.
ALTER TABLE employee_profile
    ADD CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employee_profile(id) ON DELETE SET NULL;


-- Parte 3: Criação de Índices
CREATE INDEX idx_employee_name ON employee_profile(name);
CREATE INDEX idx_employee_surname ON employee_profile(surname);
CREATE INDEX idx_employee_fiscal_number ON employee_profile(fiscal_number);
CREATE INDEX idx_employee_social_number ON employee_profile(social_number);
CREATE INDEX idx_employee_is_active ON employee_profile(is_active);
CREATE INDEX idx_employee_manager_id ON employee_profile(manager_id);
CREATE INDEX idx_employee_role ON employee_profile(employee_role);
CREATE INDEX idx_employment_type ON employee_profile(contract_role);
CREATE INDEX idx_employee_hire_date ON employee_profile(hire_date);
CREATE INDEX idx_employee_deleted_at ON employee_profile(deleted_at);

-- Parte 4: Comentários
COMMENT ON TABLE employee_profile IS 'Employee management table with soft delete support';
COMMENT ON COLUMN employee_profile.id IS 'Primary key - UUID';
COMMENT ON COLUMN employee_profile.fiscal_number IS 'Tax identification number (NIF, CPF, etc.)';
COMMENT ON COLUMN employee_profile.fiscal_number_country IS 'Country code for fiscal number (PT, BR, etc.)';
COMMENT ON COLUMN employee_profile.social_number IS 'Social security number (NISS, etc.)';
COMMENT ON COLUMN employee_profile.contract_role IS 'Employment type (FULL_TIME, PART_TIME, etc.)';
COMMENT ON COLUMN employee_profile.employee_role IS 'Employee role (MANAGER, DEVELOPER, etc.)';
COMMENT ON COLUMN employee_profile.is_active IS 'Soft delete flag - false means deleted';
COMMENT ON COLUMN employee_profile.deleted_at IS 'Timestamp when employee was soft deleted';
COMMENT ON COLUMN employee_profile.deleted_by IS 'User who performed the soft delete';
COMMENT ON COLUMN employee_profile.manager_id IS 'Foreign key to manager employee';
COMMENT ON COLUMN employee_profile.vacation_days_balance IS 'Available vacation days';
COMMENT ON COLUMN employee_profile.vacation_days_used IS 'Used vacation days';