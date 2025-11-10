-- Parte 2: Criação da Tabela vacation_request e Restrições Básicas
CREATE TABLE IF NOT EXISTS vacation_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacionamento com Employee
    employee_id UUID NOT NULL,

    -- Período das Férias
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,

    -- Status e Atividade
    vacation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Informações de Aprovação
    approving_by VARCHAR(255),
    approval_date TIMESTAMP,

    -- Observações e Rejeição
    request_notes TEXT,
    rejection_reason TEXT,

    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Chave Estrangeira
    CONSTRAINT fk_vacation_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id) ON DELETE RESTRICT,

    -- Checagens de Consistência
    CONSTRAINT chk_vacation_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_days_requested CHECK (days_requested >= 1)
);

-- Índices para melhor performance de busca e relacionamento
CREATE INDEX idx_vacation_employee_id ON vacation_request(employee_id);
CREATE INDEX idx_vacation_status ON vacation_request(vacation_status);
CREATE INDEX idx_vacation_start_date ON vacation_request(start_date);
CREATE INDEX idx_vacation_is_active ON vacation_request(is_active);
CREATE INDEX idx_vacation_deleted_at ON vacation_request(deleted_at);


-- Comentários para documentação (boas práticas)
COMMENT ON TABLE vacation_request IS 'Table for managing employee vacation requests, including status, approval, and soft delete.';
COMMENT ON COLUMN vacation_request.id IS 'Primary key - UUID';
COMMENT ON COLUMN vacation_request.employee_id IS 'Foreign key to the employee requesting vacation.';
COMMENT ON COLUMN vacation_request.start_date IS 'First day of vacation.';
COMMENT ON COLUMN vacation_request.end_date IS 'Last day of vacation.';
COMMENT ON COLUMN vacation_request.days_requested IS 'Number of days requested for vacation.';
COMMENT ON COLUMN vacation_request.vacation_status IS 'Current status of the request (PENDING, APPROVED, REJECTED, etc.).';
COMMENT ON COLUMN vacation_request.is_active IS 'Soft delete flag.';
COMMENT ON COLUMN vacation_request.approving_by IS 'User who approved or rejected the request.';
COMMENT ON COLUMN vacation_request.approval_date IS 'Timestamp of approval or rejection.';
COMMENT ON COLUMN vacation_request.request_notes IS 'Notes provided by the employee during the request.';
COMMENT ON COLUMN vacation_request.rejection_reason IS 'Reason for rejection, if applicable.';
COMMENT ON COLUMN vacation_request.deleted_at IS 'Timestamp when the request was soft deleted/cancelled.';
COMMENT ON COLUMN vacation_request.deleted_by IS 'User who performed the soft delete/cancellation.';