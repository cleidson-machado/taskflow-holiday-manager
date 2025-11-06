-- DADOS DE TESTE
-- Versão: 1.0.2 - CORRIGIDO
-- Data: 2025-11-06

-- USUÁRIOS DE TESTE
INSERT INTO user_record (id, name, surname, email, passwd, is_active, is_verified, is_locked, phone, failed_login_attempts, created_at, updated_at) VALUES
(gen_random_uuid(), 'João', 'Silva', 'joao.silva@taskflow.com', '123456', true, true, false, '+351 912 345 678', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Maria', 'Santos', 'maria.santos@taskflow.com', '123456', true, true, false, '+351 913 456 789', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pedro', 'Oliveira', 'pedro.oliveira@taskflow.com', '123456', true, true, false, '+351 914 567 890', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ana', 'Costa', 'ana.costa@taskflow.com', '123456', true, false, false, '+351 915 678 901', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Carlos', 'Ferreira', 'carlos.ferreira@taskflow.com', '123456', true, true, false, '+351 916 789 012', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Beatriz', 'Almeida', 'beatriz.almeida@taskflow.com', '123456', true, true, false, '+351 917 890 123', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ricardo', 'Pereira', 'ricardo.pereira@taskflow.com', '123456', false, true, false, '+351 918 901 234', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sofia', 'Rodrigues', 'sofia.rodrigues@taskflow.com', '123456', true, true, false, '+351 919 012 345', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Miguel', 'Martins', 'miguel.martins@taskflow.com', '123456', true, false, false, '+351 920 123 456', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Inês', 'Carvalho', 'ines.carvalho@taskflow.com', '123456', true, true, true, '+351 921 234 567', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPREGADOS DE TESTE

-- ADMIN 1 (CEO - Sem gerente) - NIF: 284447188 | NISS: 72385546187
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Fernando', 'Sousa', '284447188', '72385546187', '1975-03-15', 'FULL_TIME', 'ADMIN', '2020-01-10', true, 5500.00, NULL, 22, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ADMIN 2 (CTO - Sem gerente) - NIF: 211829900 | NISS: 82125158747
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Patrícia', 'Lopes', '211829900', '82125158747', '1978-07-22', 'FULL_TIME', 'ADMIN', '2020-02-01', true, 5200.00, NULL, 22, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- MANAGER 1 (Gerente de Operações) - NIF: 210255480 | NISS: 83379904976
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Rui', 'Mendes', '210255480', '83379904976', '1985-11-08', 'FULL_TIME', 'MANAGER', '2021-03-15', true, 3800.00, NULL, 22, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- MANAGER 2 (Gerente de TI) - NIF: 264241070 | NISS: 37594529840
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Catarina', 'Ribeiro', '264241070', '37594529840', '1988-05-19', 'FULL_TIME', 'MANAGER', '2021-06-01', true, 4000.00, NULL, 22, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 1 (Desenvolvedor) - NIF: 256093237 | NISS: 87907953250
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Tiago', 'Nunes', '256093237', '87907953250', '1992-09-12', 'FULL_TIME', 'EMPLOYEE', '2022-01-10', true, 2500.00, NULL, 22, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 2 (Analista) - NIF: 251336484 | NISS: 12668499156
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Joana', 'Pinto', '251336484', '12668499156', '1990-12-03', 'FULL_TIME', 'EMPLOYEE', '2022-03-20', true, 2400.00, NULL, 22, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 3 (Assistente Administrativo) - NIF: 299378918 | NISS: 15022324186
INSERT INTO employee_record (id, name, surname, fiscal_number, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Luís', 'Gomes', '299378918', '15022324186', '1995-04-28', 'PART_TIME', 'EMPLOYEE', '2023-02-15', true, 1800.00, NULL, 11, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);