-- ADMIN 1 (CEO - No manager) - NIF: 284447188 | NISS: 23903058881
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Fernando', 'Sousa', '284447188', 'PT', '23903058881', '1975-03-15', 'FULL_TIME', 'ADMIN', '2020-01-10', true, 5500.00, NULL, 22, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ADMIN 2 (CTO - No manager) - NIF: 211829900 | NISS: 14513842132
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Patrícia', 'Lopes', '211829900', 'PT', '14513842132', '1978-07-22', 'FULL_TIME', 'ADMIN', '2020-02-01', true, 5200.00, NULL, 22, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- MANAGER 1 (Operations Manager) - NIF: 210255480 | NISS: 16576622884
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Rui', 'Mendes', '210255480', 'PT', '16576622884', '1985-11-08', 'FULL_TIME', 'MANAGER', '2021-03-15', true, 3800.00, NULL, 22, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- MANAGER 2 (IT Manager) - NIF: 264241070 | NISS: 11028084196
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Catarina', 'Ribeiro', '264241070', 'PT', '11028084196', '1988-05-19', 'FULL_TIME', 'MANAGER', '2021-06-01', true, 4000.00, NULL, 22, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 1 (Developer) - NIF: 256093237 | NISS: 11299079043
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Tiago', 'Nunes', '256093237', 'PT', '11299079043', '1992-09-12', 'FULL_TIME', 'EMPLOYEE', '2022-01-10', true, 2500.00, NULL, 22, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 2 (Analyst) - NIF: 251336484 | NISS: 12668499156
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Joana', 'Pinto', '251336484', 'PT', '12668499156', '1990-12-03', 'FULL_TIME', 'EMPLOYEE', '2022-03-20', true, 2400.00, NULL, 22, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EMPLOYEE 3 (Administrative Assistant) - NIF: 299378918 | NISS: 15022324186
INSERT INTO employee_profile (id, name, surname, fiscal_number, fiscal_number_country, social_number, date_of_birth, contract_role, employee_role, hire_date, is_active, salary_base, manager_id, vacation_days_balance, vacation_days_used, created_at, updated_at) VALUES
(gen_random_uuid(), 'Luís', 'Gomes', '299378918', 'PT', '15022324186', '1995-04-28', 'PART_TIME', 'EMPLOYEE', '2023-02-15', true, 1800.00, NULL, 11, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);