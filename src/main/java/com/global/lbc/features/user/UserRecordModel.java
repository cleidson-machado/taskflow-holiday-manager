package com.global.lbc.features.user;

import com.global.lbc.features.human.resources.domain.model.Employee;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user record in the database.
 * This entity manages system users with authentication and authorization.
 * Relationship: One user can optionally be associated with one employee (OneToOne).
 * This class utilizes the Active Record pattern provided by Panache.
 */
@Entity
@Table(name = "user_record")
public class UserRecordModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== DADOS PESSOAIS ==========
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "surname", nullable = false, length = 100)
    public String surname;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    // ========== AUTENTICAÇÃO ==========
    /**
     * Encrypted password for user authentication.
     * Should be hashed using BCrypt or similar before storage.
     */
    @Column(name = "passwd", nullable = false, length = 255)
    public String passwd;

    // ========== RELACIONAMENTO COM EMPREGADO (OPCIONAL) ==========
    /**
     * Optional one-to-one relationship with Employee.
     * A user can be associated with an employee, but this is not mandatory.
     * Users and employees can be registered independently and linked later.
     * Uses LAZY loading to avoid unnecessary data fetching.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "employee_id", unique = true, foreignKey = @ForeignKey(name = "fk_user_employee"))
    public Employee employee;

    // ========== CONTROLE DE CONTA ==========
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    public Boolean isVerified = false;

    @Column(name = "is_locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    public Boolean isLocked = false;

    // ========== INFORMAÇÕES ADICIONAIS ==========
    @Column(name = "phone", length = 20)
    public String phone;

    @Column(name = "profile_picture_url", length = 500)
    public String profilePictureUrl;

    @Column(name = "last_login_at")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts", columnDefinition = "INTEGER DEFAULT 0")
    public Integer failedLoginAttempts = 0;

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

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Gets the full name of the user.
     *
     * @return The concatenated name and surname.
     */
    public String getFullName() {
        return name + " " + surname;
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        this.isActive = true;
        this.isLocked = false;
    }

    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Locks the user account (e.g., after too many failed login attempts).
     */
    public void lock() {
        this.isLocked = true;
    }

    /**
     * Unlocks the user account.
     */
    public void unlock() {
        this.isLocked = false;
        this.failedLoginAttempts = 0;
    }

    /**
     * Verifies the user account (e.g., after email verification).
     */
    public void verify() {
        this.isVerified = true;
    }

    /**
     * Records a successful login.
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Records a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lock();
        }
    }

    /**
     * Associates this user with an employee.
     *
     * @param employee The employee to associate with this user.
     */
    public void associateEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
     * Removes the association with an employee.
     */
    public void dissociateEmployee() {
        this.employee = null;
    }

    /**
     * Checks if this user is associated with an employee.
     *
     * @return true if associated with an employee, false otherwise.
     */
    public boolean hasEmployee() {
        return this.employee != null;
    }

    // ========== MÉTODOS DE BUSCA BÁSICOS ==========

    /**
     * Finds a user by email address.
     *
     * @param email The email address to search for.
     * @return The UserRecordModel with the specified email, or null if not found.
     */
    public static UserRecordModel findByEmail(String email) {
        return find("email", email).firstResult();
    }

    /**
     * Finds all active users.
     *
     * @return A list of active UserRecordModel.
     */
    public static List<UserRecordModel> findAllActive() {
        return list("isActive = true");
    }

    /**
     * Finds all verified users.
     *
     * @return A list of verified UserRecordModel.
     */
    public static List<UserRecordModel> findAllVerified() {
        return list("isVerified = true");
    }

    /**
     * Finds all locked users.
     *
     * @return A list of locked UserRecordModel.
     */
    public static List<UserRecordModel> findAllLocked() {
        return list("isLocked = true");
    }

    /**
     * Finds users by name (case-insensitive partial match).
     *
     * @param name The name to search for.
     * @return A list of UserRecordModel matching the name.
     */
    public static List<UserRecordModel> findByName(String name) {
        return list("LOWER(name) LIKE LOWER(?1)", "%" + name + "%");
    }

    /**
     * Finds a user by associated employee ID.
     *
     * @param employeeId The UUID of the employee.
     * @return The UserRecordModel associated with the employee, or null if not found.
     */
    public static UserRecordModel findByEmployeeId(UUID employeeId) {
        return find("employee.id", employeeId).firstResult();
    }

    /**
     * Finds all users without an associated employee.
     *
     * @return A list of UserRecordModel without an employee association.
     */
    public static List<UserRecordModel> findUsersWithoutEmployee() {
        return list("employee IS NULL");
    }

    /**
     * Finds all users with an associated employee.
     *
     * @return A list of UserRecordModel with an employee association.
     */
    public static List<UserRecordModel> findUsersWithEmployee() {
        return list("employee IS NOT NULL");
    }
}