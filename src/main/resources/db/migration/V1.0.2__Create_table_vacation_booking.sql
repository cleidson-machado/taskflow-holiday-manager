-- V1.0.2__Create_table_vacation_booking.sql
-- Cria a tabela vacation_booking e instala um trigger para manter updated_at

-- 0) (Opcional) Certifique-se da extensão para gen_random_uuid; comente se já existe
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1) Criação da tabela de bookings
CREATE TABLE IF NOT EXISTS vacation_booking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacionamentos
    employee_id UUID NOT NULL,
    vacation_id UUID NOT NULL, -- referencia a vacation_request.id

    -- Período reservado/real das férias
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_reserved INTEGER NOT NULL,

    -- Status e flags
    booking_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Observações / auditoria
    request_notes TEXT,

    -- Soft delete / quem removeu
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Auditoria de criação/atualização
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Chaves estrangeiras
    CONSTRAINT fk_booking_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_vacation FOREIGN KEY (vacation_id) REFERENCES vacation_request(id) ON DELETE RESTRICT,

    -- Checks de consistência
    CONSTRAINT chk_booking_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_days_reserved CHECK (days_reserved >= 1)
);

-- 2) Índices úteis
CREATE INDEX IF NOT EXISTS idx_booking_employee_id ON vacation_booking(employee_id);
CREATE INDEX IF NOT EXISTS idx_booking_vacation_id ON vacation_booking(vacation_id);
CREATE INDEX IF NOT EXISTS idx_booking_start_date ON vacation_booking(start_date);
CREATE INDEX IF NOT EXISTS idx_booking_end_date ON vacation_booking(end_date);
CREATE INDEX IF NOT EXISTS idx_booking_status ON vacation_booking(booking_status);
CREATE INDEX IF NOT EXISTS idx_booking_is_active ON vacation_booking(is_active);
CREATE INDEX IF NOT EXISTS idx_booking_deleted_at ON vacation_booking(deleted_at);

-- 3) Comentários (documentação)
COMMENT ON TABLE vacation_booking IS 'Table for confirmed/registered vacation bookings (links to employee_profile and vacation_request) with soft-delete support';
COMMENT ON COLUMN vacation_booking.id IS 'Primary key - UUID';
COMMENT ON COLUMN vacation_booking.employee_id IS 'FK to employee_profile.id';
COMMENT ON COLUMN vacation_booking.vacation_id IS 'FK to vacation_request.id';
COMMENT ON COLUMN vacation_booking.start_date IS 'First day of booking';
COMMENT ON COLUMN vacation_booking.end_date IS 'Last day of booking';
COMMENT ON COLUMN vacation_booking.days_reserved IS 'Number of days reserved (business may calculate working days vs calendar days)';
COMMENT ON COLUMN vacation_booking.booking_status IS 'Booking status (PENDING, CONFIRMED, CANCELLED, etc.)';
COMMENT ON COLUMN vacation_booking.is_active IS 'Soft delete flag';
COMMENT ON COLUMN vacation_booking.deleted_at IS 'Timestamp when booking was soft deleted';
COMMENT ON COLUMN vacation_booking.deleted_by IS 'User who performed the soft delete';
COMMENT ON COLUMN vacation_booking.request_notes IS 'Notes about booking';
COMMENT ON COLUMN vacation_booking.created_at IS 'Creation timestamp';
COMMENT ON COLUMN vacation_booking.updated_at IS 'Last update timestamp';

-- 4) Trigger genérico para atualizar updated_at automaticamente
-- Função (cria ou substitui)
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplica trigger nas tabelas existentes e na nova tabela.
-- Drop/create para evitar erro se o trigger já existir
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_trigger t
        JOIN pg_class c ON t.tgrelid = c.oid
        WHERE t.tgname = 'trg_employee_updated_at' AND c.relname = 'employee_profile'
    ) THEN
        EXECUTE 'DROP TRIGGER IF EXISTS trg_employee_updated_at ON employee_profile';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_trigger t
        JOIN pg_class c ON t.tgrelid = c.oid
        WHERE t.tgname = 'trg_vacation_request_updated_at' AND c.relname = 'vacation_request'
    ) THEN
        EXECUTE 'DROP TRIGGER IF EXISTS trg_vacation_request_updated_at ON vacation_request';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_trigger t
        JOIN pg_class c ON t.tgrelid = c.oid
        WHERE t.tgname = 'trg_vacation_booking_updated_at' AND c.relname = 'vacation_booking'
    ) THEN
        EXECUTE 'DROP TRIGGER IF EXISTS trg_vacation_booking_updated_at ON vacation_booking';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Cria triggers (usar BEFORE UPDATE para setar updated_at)
CREATE TRIGGER trg_employee_updated_at
BEFORE UPDATE ON employee_profile
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_vacation_request_updated_at
BEFORE UPDATE ON vacation_request
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_vacation_booking_updated_at
BEFORE UPDATE ON vacation_booking
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- 5) (Opcional) Constraint adicional para forçar days_reserved == (end_date - start_date + 1)
-- -- Habilite apenas se days_reserved deve ser sempre dias de calendário (sem excluir feriados)
-- ALTER TABLE vacation_booking
--   ADD CONSTRAINT chk_days_reserved_match CHECK (days_reserved = (end_date - start_date + 1));