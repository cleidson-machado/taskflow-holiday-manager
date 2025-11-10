package com.global.lbc.features.vacation.apparatus.model;

import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.vacation.apparatus.model.util.VacationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// ============================================================
// ENTITY: Vacation (Domain Entity with JPA Persistence)
// Follows Panache Active Record style with public fields.
// Responsible only for state and persistence lifecycle (auditing).
// All business logic and queries must reside in Services and Repositories.
// ============================================================

@Entity
@Table(name = "vacation_request")
public class Vacation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vacation_employee"))
    public Employee employee;

    // ========== PERÍODO DAS FÉRIAS ==========
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    @Column(name = "days_requested", nullable = false)
    public Integer daysRequested;

    // ========== STATUS DO PEDIDO ==========
    @Column(name = "vacation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    public VacationStatus vacationStatus = VacationStatus.PENDING;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    // ========== INFORMAÇÕES DE APROVAÇÃO ==========
    @Column(name = "approving_by", length = 255)
    public String approvingBy;

    @Column(name = "approval_date")
    public LocalDateTime approvalDate;

    // ========== OBSERVAÇÕES ==========
    @Column(name = "request_notes", columnDefinition = "TEXT")
    public String requestNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    public String rejectionReason;

    // ========== AUDITORIA - DATA E HORA DE CRIAÇÃO E ATUALIZAÇÃO ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== SOFT DELETE (Flag e Auditoria) ==========
    // soft delete fields ------------------------ start
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    public String deletedBy;
    // soft delete fields ------------------------ end

    // Ações de callback da JPA/Hibernate para Auditoria
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // GETTERS SOMENTE PARA OS CAMPOS PRIVATE (Auditoria)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

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

    public void approve(String approverName) {
        this.vacationStatus = VacationStatus.APPROVED;
        this.approvingBy = approverName;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(String approverName, String reason) {
        this.vacationStatus = VacationStatus.REJECTED;
        this.approvingBy = approverName;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    public void cancel() {
        this.vacationStatus = VacationStatus.CANCELLED;
        this.isActive = false;
    }

    public boolean isPending() {
        return this.vacationStatus == VacationStatus.PENDING;
    }

    public boolean isApproved() {
        return this.vacationStatus == VacationStatus.APPROVED;
    }
}