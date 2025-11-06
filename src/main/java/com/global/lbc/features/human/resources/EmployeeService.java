package com.global.lbc.features.human.resources;

import com.global.lbc.features.human.resources.domain.FiscalNumber;
import com.global.lbc.features.human.resources.domain.SocialNumber;
import com.global.lbc.util.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Employee business logic and validation.
 * Handles complex operations, validations, and business rules for employee management.
 */
@ApplicationScoped
public class EmployeeService {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    // ========== PAGINAÇÃO E LISTAGEM ==========

    /**
     * Retrieves paginated list of employees with sorting.
     *
     * @param page      Page number (0-indexed)
     * @param size      Number of items per page
     * @param sortField Field to sort by
     * @param sortOrder Sort order (asc/desc)
     * @return Paginated response with employee list
     */
    public PaginatedResponse<EmployeeRecordModel> getPaginatedEmployees(int page, int size, String sortField, String sortOrder) {
        validatePagination(page, size);
        validateSortField(sortField);

        Sort sortBy = buildSort(sortField, sortOrder);

        var query = EmployeeRecordModel.findAll(sortBy).page(page, size);
        long totalItems = EmployeeRecordModel.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PaginatedResponse<>(
                query.list(),
                totalItems,
                totalPages,
                page
        );
    }

    /**
     * Retrieves the first 50 active employees.
     *
     * @return List of up to 50 active employees
     */
    public List<EmployeeRecordModel> getFirst50ActiveEmployees() {
        return EmployeeRecordModel.find("isActive = true", Sort.by("name").ascending())
                .page(0, DEFAULT_PAGE_SIZE)
                .list();
    }

    /**
     * Retrieves all active employees with pagination.
     *
     * @param page Page number
     * @param size Page size
     * @return Paginated response with active employees
     */
    public PaginatedResponse<EmployeeRecordModel> getActiveEmployees(int page, int size) {
        validatePagination(page, size);

        var query = EmployeeRecordModel.find("isActive = true", Sort.by("name").ascending())
                .page(page, size);
        long totalItems = EmployeeRecordModel.count("isActive = true");
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PaginatedResponse<>(
                query.list(),
                totalItems,
                totalPages,
                page
        );
    }

    // ========== BUSCA E CONSULTA ==========

    /**
     * Finds an employee by ID.
     *
     * @param id Employee UUID
     * @return Optional containing the employee if found
     */
    public Optional<EmployeeRecordModel> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return Optional.ofNullable(EmployeeRecordModel.findById(id));
    }

    /**
     * Searches employees by name or surname.
     *
     * @param searchTerm Search term
     * @return List of matching employees
     */
    public List<EmployeeRecordModel> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new IllegalArgumentException("Termo de busca não pode ser vazio");
        }
        return EmployeeRecordModel.searchByName(searchTerm);
    }

    /**
     * Finds employees by role.
     *
     * @param role Employee role
     * @return List of employees with the specified role
     */
    public List<EmployeeRecordModel> findByRole(EmployeeRoleEnum role) {
        if (role == null) {
            throw new IllegalArgumentException("Role não pode ser nulo");
        }
        return EmployeeRecordModel.findByRole(role);
    }

    /**
     * Finds employees by contract type.
     *
     * @param contractType Contract type
     * @return List of employees with the specified contract type
     */
    public List<EmployeeRecordModel> findByContractType(TypeOfContractEnum contractType) {
        if (contractType == null) {
            throw new IllegalArgumentException("Tipo de contrato não pode ser nulo");
        }
        return EmployeeRecordModel.list("contractRole", contractType);
    }

    // ========== VALIDAÇÕES DE UNICIDADE ==========

    /**
     * Checks if a fiscal number (NIF) is already registered.
     *
     * @param fiscalNumber Fiscal number to check
     * @return true if already exists, false otherwise
     */
    public boolean existsByFiscalNumber(FiscalNumber fiscalNumber) {
        if (fiscalNumber == null) {
            return false;
        }
        return EmployeeRecordModel.count("fiscalNumber = ?1", fiscalNumber) > 0;
    }

    /**
     * Checks if a fiscal number exists for a different employee (useful for updates).
     *
     * @param fiscalNumber Fiscal number to check
     * @param excludeId    Employee ID to exclude from check
     * @return true if exists for another employee
     */
    public boolean existsByFiscalNumberExcludingId(FiscalNumber fiscalNumber, UUID excludeId) {
        if (fiscalNumber == null) {
            return false;
        }
        return EmployeeRecordModel.count("fiscalNumber = ?1 and id != ?2", fiscalNumber, excludeId) > 0;
    }

    /**
     * Checks if a social number (NISS) is already registered.
     *
     * @param socialNumber Social number to check
     * @return true if already exists, false otherwise
     */
    public boolean existsBySocialNumber(SocialNumber socialNumber) {
        if (socialNumber == null) {
            return false;
        }
        return EmployeeRecordModel.count("socialNumber = ?1", socialNumber) > 0;
    }

    /**
     * Checks if a social number exists for a different employee (useful for updates).
     *
     * @param socialNumber Social number to check
     * @param excludeId    Employee ID to exclude from check
     * @return true if exists for another employee
     */
    public boolean existsBySocialNumberExcludingId(SocialNumber socialNumber, UUID excludeId) {
        if (socialNumber == null) {
            return false;
        }
        return EmployeeRecordModel.count("socialNumber = ?1 and id != ?2", socialNumber, excludeId) > 0;
    }

    /**
     * Validates that fiscal and social numbers are unique before creating/updating an employee.
     *
     * @param fiscalNumber Fiscal number to validate
     * @param socialNumber Social number to validate
     * @param excludeId    Employee ID to exclude (null for new employees)
     * @throws IllegalArgumentException if numbers are already in use
     */
    public void validateUniqueIdentifiers(FiscalNumber fiscalNumber, SocialNumber socialNumber, UUID excludeId) {
        if (fiscalNumber != null) {
            boolean fiscalExists = (excludeId == null)
                    ? existsByFiscalNumber(fiscalNumber)
                    : existsByFiscalNumberExcludingId(fiscalNumber, excludeId);

            if (fiscalExists) {
                throw new IllegalArgumentException("NIF já cadastrado: " + fiscalNumber.getValue());
            }
        }

        if (socialNumber != null) {
            boolean socialExists = (excludeId == null)
                    ? existsBySocialNumber(socialNumber)
                    : existsBySocialNumberExcludingId(socialNumber, excludeId);

            if (socialExists) {
                throw new IllegalArgumentException("NISS já cadastrado: " + socialNumber.getValue());
            }
        }
    }

    // ========== OPERAÇÕES DE CRIAÇÃO E ATUALIZAÇÃO ==========

    /**
     * Creates a new employee with validation.
     *
     * @param employee Employee to create
     * @return Created employee
     */
    @Transactional
    public EmployeeRecordModel createEmployee(EmployeeRecordModel employee) {
        validateEmployeeData(employee);
        validateUniqueIdentifiers(employee.fiscalNumber, employee.socialNumber, null);

        if (employee.manager != null) {
            validateManagerAssignment(null, employee.manager.id);
        }

        employee.persist();
        return employee;
    }

    /**
     * Updates an existing employee with validation.
     *
     * @param id       Employee ID
     * @param employee Updated employee data
     * @return Updated employee
     */
    @Transactional
    public EmployeeRecordModel updateEmployee(UUID id, EmployeeRecordModel employee) {
        EmployeeRecordModel existing = EmployeeRecordModel.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Empregado não encontrado: " + id);
        }

        validateEmployeeData(employee);
        validateUniqueIdentifiers(employee.fiscalNumber, employee.socialNumber, id);

        if (employee.manager != null) {
            validateManagerAssignment(id, employee.manager.id);
        }

        // Update fields
        existing.name = employee.name;
        existing.surname = employee.surname;
        existing.fiscalNumber = employee.fiscalNumber;
        existing.socialNumber = employee.socialNumber;
        existing.dateOfBirth = employee.dateOfBirth;
        existing.contractRole = employee.contractRole;
        existing.employeeRole = employee.employeeRole;
        existing.hireDate = employee.hireDate;
        existing.terminationDate = employee.terminationDate;
        existing.isActive = employee.isActive;
        existing.salaryBase = employee.salaryBase;
        existing.manager = employee.manager;
        existing.vacationDaysBalance = employee.vacationDaysBalance;
        existing.vacationDaysUsed = employee.vacationDaysUsed;

        return existing;
    }

    /**
     * Deactivates an employee (soft delete).
     *
     * @param id Employee ID
     */
    @Transactional
    public void deactivateEmployee(UUID id) {
        EmployeeRecordModel employee = EmployeeRecordModel.findById(id);
        if (employee == null) {
            throw new IllegalArgumentException("Empregado não encontrado: " + id);
        }

        employee.isActive = false;
        employee.terminationDate = LocalDate.now();
    }

    /**
     * Permanently deletes an employee.
     *
     * @param id Employee ID
     */
    @Transactional
    public void deleteEmployee(UUID id) {
        EmployeeRecordModel employee = EmployeeRecordModel.findById(id);
        if (employee == null) {
            throw new IllegalArgumentException("Empregado não encontrado: " + id);
        }

        // Check if employee is a manager
        if (employee.isManager()) {
            throw new IllegalArgumentException("Não é possível excluir um empregado que é gerente. Reatribua os subordinados primeiro.");
        }

        employee.delete();
    }

    // ========== GESTÃO DE HIERARQUIA ==========

    /**
     * Assigns a manager to an employee.
     *
     * @param employeeId Employee ID
     * @param managerId  Manager ID
     */
    @Transactional
    public void assignManager(UUID employeeId, UUID managerId) {
        validateManagerAssignment(employeeId, managerId);

        EmployeeRecordModel employee = EmployeeRecordModel.findById(employeeId);
        EmployeeRecordModel manager = EmployeeRecordModel.findById(managerId);

        if (employee == null || manager == null) {
            throw new IllegalArgumentException("Empregado ou gerente não encontrado");
        }

        employee.manager = manager;
    }

    /**
     * Removes manager from an employee.
     *
     * @param employeeId Employee ID
     */
    @Transactional
    public void removeManager(UUID employeeId) {
        EmployeeRecordModel employee = EmployeeRecordModel.findById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Empregado não encontrado: " + employeeId);
        }

        employee.manager = null;
    }

    /**
     * Gets all subordinates of a manager.
     *
     * @param managerId Manager ID
     * @return List of subordinates
     */
    public List<EmployeeRecordModel> getSubordinates(UUID managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("ID do gerente não pode ser nulo");
        }
        return EmployeeRecordModel.findSubordinatesByManager(managerId);
    }

    /**
     * Gets all managers in the organization.
     *
     * @return List of managers
     */
    public List<EmployeeRecordModel> getAllManagers() {
        return EmployeeRecordModel.findAllManagers();
    }

    /**
     * Gets top-level employees (without managers).
     *
     * @return List of top-level employees
     */
    public List<EmployeeRecordModel> getTopLevelEmployees() {
        return EmployeeRecordModel.findTopLevelEmployees();
    }

    // ========== VALIDAÇÕES PRIVADAS ==========

    private void validateEmployeeData(EmployeeRecordModel employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Dados do empregado não podem ser nulos");
        }

        if (employee.name == null || employee.name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (employee.surname == null || employee.surname.isBlank()) {
            throw new IllegalArgumentException("Sobrenome é obrigatório");
        }

        if (employee.contractRole == null) {
            throw new IllegalArgumentException("Tipo de contrato é obrigatório");
        }

        if (employee.employeeRole == null) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }

        if (employee.hireDate == null) {
            throw new IllegalArgumentException("Data de contratação é obrigatória");
        }

        if (employee.hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de contratação não pode ser futura");
        }

        if (employee.dateOfBirth != null && employee.dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser futura");
        }

        if (employee.terminationDate != null && employee.terminationDate.isBefore(employee.hireDate)) {
            throw new IllegalArgumentException("Data de término não pode ser anterior à data de contratação");
        }

        if (employee.salaryBase != null && employee.salaryBase < 0) {
            throw new IllegalArgumentException("Salário base não pode ser negativo");
        }
    }

    private void validateManagerAssignment(UUID employeeId, UUID managerId) {
        if (managerId == null) {
            return; // Removing manager is valid
        }

        if (employeeId != null && !EmployeeRecordModel.canAssignManager(employeeId, managerId)) {
            throw new IllegalArgumentException("Atribuição de gerente inválida: referência circular ou auto-atribuição detectada");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Número da página não pode ser negativo");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que zero");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Tamanho da página não pode exceder " + MAX_PAGE_SIZE);
        }
    }

    private void validateSortField(String field) {
        if (!isValidSortField(field)) {
            throw new IllegalArgumentException("Campo de ordenação inválido: " + field);
        }
    }

    private boolean isValidSortField(String field) {
        return List.of(
                "name",
                "surname",
                "hireDate",
                "employeeRole",
                "contractRole",
                "isActive",
                "createdAt",
                "dateOfBirth",
                "salaryBase"
        ).contains(field);
    }

    private Sort buildSort(String sortField, String sortOrder) {
        return "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
    }

    // ========== ESTATÍSTICAS E RELATÓRIOS ==========

    /**
     * Gets total count of active employees.
     *
     * @return Count of active employees
     */
    public long countActiveEmployees() {
        return EmployeeRecordModel.count("isActive = true");
    }

    /**
     * Gets total count of employees by role.
     *
     * @param role Employee role
     * @return Count of employees with specified role
     */
    public long countByRole(EmployeeRoleEnum role) {
        return EmployeeRecordModel.count("employeeRole = ?1", role);
    }

    /**
     * Gets total count of employees by contract type.
     *
     * @param contractType Contract type
     * @return Count of employees with specified contract type
     */
    public long countByContractType(TypeOfContractEnum contractType) {
        return EmployeeRecordModel.count("contractRole = ?1", contractType);
    }

    /**
     * Gets employees hired within a date range.
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return List of employees hired in the range
     */
    public List<EmployeeRecordModel> findByHireDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }

        return EmployeeRecordModel.list("hireDate >= ?1 and hireDate <= ?2", startDate, endDate);
    }
}