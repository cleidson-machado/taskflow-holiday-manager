package com.global.lbc.features.auth.apparatus.model;

import com.global.lbc.features.employee.apparatus.model.Employee;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_user_profile")
public class AuthUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "surname", nullable = false, length = 100)
    public String surname;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "passwd", nullable = false, length = 255)
    public String passwd;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "employee_id", unique = true, foreignKey = @ForeignKey(name = "fk_user_employee"))
    public Employee employeeRecordModel;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    public Boolean isVerified = false;

    @Column(name = "is_locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    public Boolean isLocked = false;

    @Column(name = "phone", length = 20)
    public String phone;

    @Column(name = "profile_picture_url", length = 500)
    public String profilePictureUrl;

    @Column(name = "last_login_at")
    public LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts", columnDefinition = "INTEGER DEFAULT 0")
    public Integer failedLoginAttempts = 0;

    // ========== SOFT DELETE (Flag e Auditoria) ==========
    // soft delete fields ------------------------ start
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    public String deletedBy;
    // soft delete fields ------------------------ end

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Convenience method to perform a soft delete and record who performed the action.
     * It is recommended that Services call this method (passing the user) instead of
     * directly invoking deletion operations that do not record the deletedBy user.
     */
    public void softDelete(String deletedBy) {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
