-- V1.0.4__Fix_vacation_booking_vacation_id_nullable.sql
-- Corrige a coluna vacation_id para aceitar valores nulos
-- Um booking pode ser criado antes de ser vinculado a uma vacation

ALTER TABLE vacation_booking
    ALTER COLUMN vacation_id DROP NOT NULL;

-- Remove a constraint de foreign key antiga e recria permitindo NULL
ALTER TABLE vacation_booking
    DROP CONSTRAINT IF EXISTS fk_booking_vacation;

-- Recria a constraint permitindo valores nulos
ALTER TABLE vacation_booking
    ADD CONSTRAINT fk_booking_vacation
    FOREIGN KEY (vacation_id)
    REFERENCES vacation_request(id)
    ON DELETE SET NULL;

-- Altera o status padrão para RESERVED ao invés de PENDING
ALTER TABLE vacation_booking
    ALTER COLUMN booking_status SET DEFAULT 'RESERVED';
