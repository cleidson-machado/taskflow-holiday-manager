package com.global.lbc.features.human.resources;

import com.global.lbc.features.human.resources.domain.FiscalNumber;
import com.global.lbc.features.human.resources.domain.SocialNumber;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an employee record in the database with self-referencing manager-subordinate relationship.
 * This class utilizes the Active Record pattern provided by Panache.
 *
 * Implements Soft Delete pattern for data retention and audit compliance.
 */
@Entity
@Table(name = "employee_record")
public class EmployeeRecordModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== INFORMAÇÕES PESSOAIS ==========
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "surname", nullable = false, length = 100)
    public String surname;

    @Column(name = "fiscal_number", unique = true, length = 20)
    public FiscalNumber fiscalNumber;

    @Column(name = "social_number", unique = true, length = 20)
    public SocialNumber socialNumber;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    // ========== INFORMAÇÕES CONTRATUAIS ==========
    @Column(name = "contract_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public TypeOfContractEnum contractRole;

    @Column(name = "employee_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public EmployeeRoleEnum employeeRole;

    @Column(name = "hire_date", nullable = false)
    public LocalDate hireDate;

    @Column(name = "termination_date")
    public LocalDate terminationDate;

    @Column(name = "salary_base")
    public Float salaryBase;

    // ========== SOFT DELETE FIELDS ==========
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    public String deletedBy;

    // ========== AUTO-RELACIONAMENTO: HIERARQUIA GERENCIAL ==========

    /**
     * Manager of this employee. A manager is also an employee.
     * Uses LAZY loading to avoid loading the entire hierarchy at once.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    public EmployeeRecordModel manager;

    /**
     * List of subordinates (employees) managed by this employee.
     * Only populated when this employee acts as a manager.
     */
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<EmployeeRecordModel> subordinates = new HashSet<>();

    // ========== DEPARTAMENTO ==========
    // TODO: Add department field/relationship when Department entity is created
    // @ManyToOne
    // @JoinColumn(name = "department_id")
    // public DepartmentModel department;

    // ========== FÉRIAS ==========
    @Column(name = "vacation_days_balance", columnDefinition = "BIGINT DEFAULT 0")
    public Long vacationDaysBalance = 0L;

    @Column(name = "vacation_days_used", columnDefinition = "BIGINT DEFAULT 0")
    public Long vacationDaysUsed = 0L;

    // ========== AUDITORIA - DATA E HORA DE CRIAÇÃO E ATUALIZAÇÃO ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    /**
     * Automatically sets the creation timestamp before persisting the entity.
     * This method is called by JPA before the entity is inserted into the database.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Automatically updates the modification timestamp before updating the entity.
     * This method is called by JPA before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS E SETTERS PARA AUDITORIA ==========

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== SOFT DELETE UTILITIES ==========

    /**
     * Marca o registro como deletado (soft delete).
     * Preserva os dados no banco para auditoria e possível restauração.
     *
     * @param deletedBy Username ou identificador do usuário que executou a deleção
     */
    public void softDelete(String deletedBy) {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restaura um registro soft-deleted.
     * Remove as marcações de deleção e reativa o registro.
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Verifica se o registro está deletado (soft delete).
     *
     * @return true se o registro está marcado como deletado
     */
    public boolean isDeleted() {
        return !isActive || deletedAt != null;
    }

    // ========== MÉTODOS AUXILIARES PARA GERENCIAR RELACIONAMENTO BIDIRECIONAL ==========

    /**
     * Adds a subordinate to this manager and establishes the bidirectional relationship.
     * Validates that an employee cannot manage themselves.
     *
     * @param subordinate The employee to be added as subordinate.
     * @throws IllegalArgumentException if trying to add self as subordinate.
     */
    public void addSubordinate(EmployeeRecordModel subordinate) {
        if (this.id != null && this.id.equals(subordinate.id)) {
            throw new IllegalArgumentException("Um empregado não pode ser gerente de si mesmo");
        }
        subordinates.add(subordinate);
        subordinate.manager = this;
    }

    /**
     * Removes a subordinate from this manager and breaks the bidirectional relationship.
     *
     * @param subordinate The employee to be removed from subordinates.
     */
    public void removeSubordinate(EmployeeRecordModel subordinate) {
        subordinates.remove(subordinate);
        subordinate.manager = null;
    }

    /**
     * Checks if this employee has a manager assigned.
     *
     * @return true if this employee has a manager, false otherwise.
     */
    public boolean hasManager() {
        return this.manager != null;
    }

    /**
     * Checks if this employee is a manager (has subordinates).
     *
     * @return true if this employee has subordinates, false otherwise.
     */
    public boolean isManager() {
        return !this.subordinates.isEmpty();
    }

    // ========== MÉTODOS DE BUSCA (COM SOFT DELETE) ==========

    /**
     * Finds an employee by fiscal number (only active employees).
     *
     * @param fiscalNumber The fiscal number to search for.
     * @return The found EmployeeRecordModel, or null if not found.
     */
    public static EmployeeRecordModel findByFiscalNumber(String fiscalNumber) {
        return find("fiscalNumber = ?1 AND isActive = true", fiscalNumber).firstResult();
    }

    /**
     * Finds an employee by social number (only active employees).
     *
     * @param socialNumber The social number to search for.
     * @return The found EmployeeRecordModel, or null if not found.
     */
    public static EmployeeRecordModel findBySocialNumber(String socialNumber) {
        return find("socialNumber = ?1 AND isActive = true", socialNumber).firstResult();
    }

    /**
     * Searches for employees by name or surname (case-insensitive, partial match).
     * Only returns active employees.
     *
     * @param searchTerm The partial term to search for.
     * @return A list of EmployeeRecordModel matching the criteria.
     */
    public static List<EmployeeRecordModel> searchByName(String searchTerm) {
        return list("(LOWER(name) LIKE LOWER(?1) OR LOWER(surname) LIKE LOWER(?1)) AND isActive = true",
                "%" + searchTerm.toLowerCase() + "%");
    }

    /**
     * Finds all active employees.
     *
     * @return A list of active EmployeeRecordModel.
     */
    public static List<EmployeeRecordModel> findAllActive() {
        return list("isActive = true");
    }

    /**
     * Finds all employees by role (only active).
     *
     * @param role The employee role to search for.
     * @return A list of EmployeeRecordModel with the specified role.
     */
    public static List<EmployeeRecordModel> findByRole(EmployeeRoleEnum role) {
        return list("employeeRole = ?1 AND isActive = true", role);
    }

    /**
     * Finds all subordinates of a specific manager (only active).
     *
     * @param managerId The UUID of the manager.
     * @return A list of EmployeeRecordModel who report to the specified manager.
     */
    public static List<EmployeeRecordModel> findSubordinatesByManager(UUID managerId) {
        return list("manager.id = ?1 AND isActive = true", managerId);
    }

    /**
     * Finds all employees who are managers (have at least one subordinate).
     * Uses a query to check for employees referenced as managers.
     * Only returns active managers.
     *
     * @return A list of EmployeeRecordModel who are managers.
     */
    public static List<EmployeeRecordModel> findAllManagers() {
        return list("SELECT DISTINCT e FROM EmployeeRecordModel e WHERE e.id IN " +
                "(SELECT m.manager.id FROM EmployeeRecordModel m WHERE m.manager IS NOT NULL AND m.isActive = true) " +
                "AND e.isActive = true");
    }

    /**
     * Finds all employees without a manager (top-level employees).
     * Only returns active employees.
     *
     * @return A list of EmployeeRecordModel without managers.
     */
    public static List<EmployeeRecordModel> findTopLevelEmployees() {
        return list("manager IS NULL AND isActive = true");
    }

    // ========== MÉTODOS DE BUSCA PARA SOFT-DELETED (ADMIN) ==========

    /**
     * Busca todos os registros deletados (soft delete).
     * Método restrito para administradores.
     *
     * @return Lista de empregados marcados como deletados
     */
    public static List<EmployeeRecordModel> findAllDeleted() {
        return list("isActive = false");
    }

    /**
     * Busca um empregado por ID incluindo registros deletados.
     * Útil para operações administrativas de restauração.
     *
     * @param id UUID do empregado
     * @return EmployeeRecordModel ou null se não encontrado
     */
    public static EmployeeRecordModel findByIdIncludingDeleted(UUID id) {
        return findById(id); // Não filtra isActive
    }

    /**
     * Busca empregados deletados em um período específico.
     *
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de empregados deletados no período
     */
    public static List<EmployeeRecordModel> findDeletedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return list("isActive = false AND deletedAt BETWEEN ?1 AND ?2", startDate, endDate);
    }

    /**
     * Busca empregados deletados por um usuário específico.
     *
     * @param deletedBy Username do usuário que executou a deleção
     * @return Lista de empregados deletados pelo usuário
     */
    public static List<EmployeeRecordModel> findDeletedBy(String deletedBy) {
        return list("isActive = false AND deletedBy = ?1", deletedBy);
    }

    // ========== VALIDAÇÕES ==========

    /**
     * Validates if an employee can be assigned as manager of another employee.
     * Prevents circular references and self-management.
     *
     * @param employeeId The ID of the employee to be managed.
     * @param managerId The ID of the potential manager.
     * @return true if the assignment is valid, false otherwise.
     */
    public static boolean canAssignManager(UUID employeeId, UUID managerId) {
        if (employeeId.equals(managerId)) {
            return false; // Cannot manage self
        }

        // Check for circular reference: if managerId is a subordinate of employeeId
        EmployeeRecordModel potentialManager = findById(managerId);
        if (potentialManager != null && potentialManager.manager != null) {
            return !isSubordinateOf(potentialManager.manager.id, employeeId);
        }

        return true;
    }

    /**
     * Helper method to check if an employee is a subordinate (direct or indirect) of another.
     *
     * @param employeeId The employee to check.
     * @param potentialSuperiorId The potential superior in the hierarchy.
     * @return true if employeeId is a subordinate of potentialSuperiorId.
     */
    private static boolean isSubordinateOf(UUID employeeId, UUID potentialSuperiorId) {
        EmployeeRecordModel employee = findById(employeeId);
        while (employee != null && employee.manager != null) {
            if (employee.manager.id.equals(potentialSuperiorId)) {
                return true;
            }
            employee = employee.manager;
        }
        return false;
    }

    /**
     * Conta quantos subordinados ativos um gerente possui.
     * Útil para validar se um gerente pode ser deletado.
     *
     * @param managerId UUID do gerente
     * @return Número de subordinados ativos
     */
    public static long countActiveSubordinates(UUID managerId) {
        return count("manager.id = ?1 AND isActive = true", managerId);
    }

    /**
     * Verifica se um empregado pode ser deletado (soft delete).
     * Gerentes com subordinados ativos não podem ser deletados.
     *
     * @return true se pode ser deletado
     */
    public boolean canBeDeleted() {
        if (!this.isManager()) {
            return true;
        }
        return countActiveSubordinates(this.id) == 0;
    }
}