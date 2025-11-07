package com.global.lbc.features.vacation.reservation;

import com.global.lbc.features.human.resources.domain.model.Employee;
import com.global.lbc.features.vacation.order.VacationStatusEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a vacation reservation record in the database.
 * This entity manages employee vacation planning/reservations to prevent conflicts.
 * Relationship: Many vacation reservations can belong to one employee (ManyToOne).
 * This class utilizes the Active Record pattern provided by Panache.
 */
@Entity
@Table(name = "vacation_reservation")
public class VacationReservationModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== RELACIONAMENTO COM EMPREGADO ==========
    /**
     * The employee who created this vacation reservation.
     * Many vacation reservations can belong to one employee.
     * Uses LAZY loading to avoid unnecessary data fetching.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_employee"))
    public Employee employee;

    // ========== PERÍODO DA RESERVA ==========
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    @Column(name = "days_requested", nullable = false)
    public Long daysRequested = 0L;

    // ========== STATUS DA RESERVA ==========
    @Column(name = "reservation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    public VacationStatusEnum status = VacationStatusEnum.PENDING;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    @Column(name = "is_simulation", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    public Boolean isSimulation = false;

    // ========== INFORMAÇÕES DE APROVAÇÃO ==========
    /**
     * The manager or coworker who approved/rejected this reservation.
     * Stores the approver's employee ID or name.
     */
    @Column(name = "approving_by", length = 255)
    public String approvingBy;

    @Column(name = "approved_at")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime rejectedAt;

    // ========== INFORMAÇÕES DE CANCELAMENTO ==========
    /**
     * The employee who withdrew/cancelled this reservation.
     */
    @Column(name = "withdrawal_by", length = 255)
    public String withdrawalBy;

    // ========== OBSERVAÇÕES ==========
    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

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

        // Calculate days requested if not set
        if (daysRequested == null || daysRequested == 0L) {
            if (startDate != null && endDate != null) {
                daysRequested = calculateDays(startDate, endDate);
            }
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

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Calculates the number of days between two dates.
     * This is a simplified calculation that counts all days between start and end.
     *
     * @param start The start date of reservation.
     * @param end The end date of reservation.
     * @return The number of days between start and end (inclusive).
     */
    private Long calculateDays(LocalDate start, LocalDate end) {
        return end.toEpochDay() - start.toEpochDay() + 1;
    }

    /**
     * Approves this vacation reservation.
     *
     * @param approverName The name or ID of the person approving the reservation.
     */
    public void approve(String approverName) {
        this.status = VacationStatusEnum.APPROVED;
        this.approvingBy = approverName;
        this.approvedAt = LocalDateTime.now();
        this.rejectedAt = null;
    }

    /**
     * Rejects this vacation reservation.
     *
     * @param approverName The name or ID of the person rejecting the reservation.
     */
    public void reject(String approverName) {
        this.status = VacationStatusEnum.REJECTED;
        this.approvingBy = approverName;
        this.rejectedAt = LocalDateTime.now();
        this.approvedAt = null;
    }

    /**
     * Cancels this vacation reservation.
     *
     * @param withdrawnBy The name or ID of the person cancelling the reservation.
     */
    public void cancel(String withdrawnBy) {
        this.status = VacationStatusEnum.CANCELLED;
        this.withdrawalBy = withdrawnBy;
        this.isActive = false;
    }

    /**
     * Checks if this vacation reservation is pending approval.
     *
     * @return true if status is PENDING, false otherwise.
     */
    public boolean isPending() {
        return this.status == VacationStatusEnum.PENDING;
    }

    /**
     * Checks if this vacation reservation has been approved.
     *
     * @return true if status is APPROVED, false otherwise.
     */
    public boolean isApproved() {
        return this.status == VacationStatusEnum.APPROVED;
    }

    // ========== MÉTODOS DE BUSCA BÁSICOS ==========

    /**
     * Finds all vacation reservations for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @return A list of VacationReservationModel for the specified employee.
     */
    public static List<VacationReservationModel> findByEmployee(UUID employeeId) {
        return list("employee.id", employeeId);
    }

    /**
     * Finds all active vacation reservations for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @return A list of active VacationReservationModel for the specified employee.
     */
    public static List<VacationReservationModel> findActiveByEmployee(UUID employeeId) {
        return list("employee.id = ?1 and isActive = true", employeeId);
    }

    /**
     * Finds all vacation reservations by status.
     *
     * @param status The vacation status to search for.
     * @return A list of VacationReservationModel with the specified status.
     */
    public static List<VacationReservationModel> findByStatus(VacationStatusEnum status) {
        return list("status", status);
    }

    /**
     * Finds all pending vacation reservations.
     *
     * @return A list of pending VacationReservationModel.
     */
    public static List<VacationReservationModel> findAllPending() {
        return list("status = ?1 and isActive = true", VacationStatusEnum.PENDING);
    }

    /**
     * Finds vacation reservations within a date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of VacationReservationModel within the specified date range.
     */
    public static List<VacationReservationModel> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return list("(startDate between ?1 and ?2) or (endDate between ?1 and ?2)",
                startDate, endDate);
    }
}