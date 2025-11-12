-- SCRIPT DE CORREÇÃO MANUAL (caso o Flyway já tenha executado a V1.0.2)
-- Execute este script se você encontrar o erro de constraint violation
-- Depois delete a tabela flyway_schema_history para a versão V1.0.2 e execute novamente

-- Opção 1: Se você quiser apenas corrigir a tabela existente
ALTER TABLE vacation_booking
    ALTER COLUMN vacation_id DROP NOT NULL;

ALTER TABLE vacation_booking
    DROP CONSTRAINT IF EXISTS fk_booking_vacation;

ALTER TABLE vacation_booking
    ADD CONSTRAINT fk_booking_vacation
    FOREIGN KEY (vacation_id)
    REFERENCES vacation_request(id)
    ON DELETE SET NULL;

ALTER TABLE vacation_booking
    ALTER COLUMN booking_status SET DEFAULT 'RESERVED';

-- Opção 2: Se preferir recriar a tabela completamente (CUIDADO: vai perder dados)
-- DROP TABLE IF EXISTS vacation_booking CASCADE;
-- Depois execute: ./mvnw flyway:migrate

