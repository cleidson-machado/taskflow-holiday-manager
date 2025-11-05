package com.global.lbc.features.vacation.order;

import com.global.lbc.features.human.resources.EmployeeRecordModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a vacation request record in the database.
 * This entity manages employee vacation requests with approval workflow.
 * Relationship: Many vacation requests can belong to one employee (ManyToOne).
 * This class utilizes the Active Record pattern provided by Panache.
 */
@Entity
@Table(name = "vacation_request")
public class VacationRequestModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== RELACIONAMENTO COM EMPREGADO ==========
    /**
     * The employee who submitted this vacation request.
     * Many vacation requests can belong to one employee.
     * Uses LAZY loading to avoid unnecessary data fetching.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vacation_employee"))
    public EmployeeRecordModel employee;

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
    public VacationStatusEnum vacationStatus = VacationStatusEnum.PENDING;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    // ========== INFORMAÇÕES DE APROVAÇÃO ==========
    /**
     * The manager or HR person who approved/rejected this request.
     * Stores the approver's employee ID or name.
     */
    @Column(name = "approving_by", length = 255)
    public String approvingBy;

    @Column(name = "approval_date")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime approvalDate;

    // ========== OBSERVAÇÕES ==========
    @Column(name = "request_notes", columnDefinition = "TEXT")
    public String requestNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    public String rejectionReason;

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
        if (daysRequested == null && startDate != null && endDate != null) {
            daysRequested = calculateBusinessDays(startDate, endDate);
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
     * Calculates the number of business days between two dates.
     * This is a simplified calculation that counts all days between start and end.
     * For production, consider using a library that handles holidays and weekends.
     *
     * @param start The start date of vacation.
     * @param end The end date of vacation.
     * @return The number of days between start and end (inclusive).
     */
    private int calculateBusinessDays(LocalDate start, LocalDate end) {
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }

    /**
     * Approves this vacation request.
     *
     * @param approverName The name or ID of the person approving the request.
     */
    public void approve(String approverName) {
        this.vacationStatus = VacationStatusEnum.APPROVED;
        this.approvingBy = approverName;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = null;
    }

    /**
     * Rejects this vacation request.
     *
     * @param approverName The name or ID of the person rejecting the request.
     * @param reason The reason for rejection.
     */
    public void reject(String approverName, String reason) {
        this.vacationStatus = VacationStatusEnum.REJECTED;
        this.approvingBy = approverName;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Cancels this vacation request.
     */
    public void cancel() {
        this.vacationStatus = VacationStatusEnum.CANCELLED;
        this.isActive = false;
    }

    /**
     * Checks if this vacation request is pending approval.
     *
     * @return true if status is PENDING, false otherwise.
     */
    public boolean isPending() {
        return this.vacationStatus == VacationStatusEnum.PENDING;
    }

    /**
     * Checks if this vacation request has been approved.
     *
     * @return true if status is APPROVED, false otherwise.
     */
    public boolean isApproved() {
        return this.vacationStatus == VacationStatusEnum.APPROVED;
    }

    // ========== MÉTODOS DE BUSCA ==========

    /**
     * Finds all vacation requests for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @return A list of VacationRequestModel for the specified employee.
     */
    public static List<VacationRequestModel> findByEmployee(UUID employeeId) {
        return list("employee.id", employeeId);
    }

    /**
     * Finds all active vacation requests for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @return A list of active VacationRequestModel for the specified employee.
     */
    public static List<VacationRequestModel> findActiveByEmployee(UUID employeeId) {
        return list("employee.id = ?1 and isActive = true", employeeId);
    }

    /**
     * Finds all vacation requests by status.
     *
     * @param status The vacation status to search for.
     * @return A list of VacationRequestModel with the specified status.
     */
    public static List<VacationRequestModel> findByStatus(VacationStatusEnum status) {
        return list("vacationStatus", status);
    }

    /**
     * Finds all pending vacation requests.
     *
     * @return A list of pending VacationRequestModel.
     */
    public static List<VacationRequestModel> findAllPending() {
        return list("vacationStatus = ?1 and isActive = true", VacationStatusEnum.PENDING);
    }

    /**
     * Finds all approved vacation requests for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @return A list of approved VacationRequestModel.
     */
    public static List<VacationRequestModel> findApprovedByEmployee(UUID employeeId) {
        return list("employee.id = ?1 and vacationStatus = ?2",
                employeeId, VacationStatusEnum.APPROVED);
    }

    /**
     * Finds vacation requests within a date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of VacationRequestModel within the specified date range.
     */
    public static List<VacationRequestModel> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return list("(startDate between ?1 and ?2) or (endDate between ?1 and ?2)",
                startDate, endDate);
    }

    /**
     * Finds overlapping vacation requests for a specific employee.
     * Useful for validating that an employee doesn't have overlapping vacation periods.
     *
     * @param employeeId The UUID of the employee.
     * @param startDate The start date to check.
     * @param endDate The end date to check.
     * @return A list of overlapping VacationRequestModel.
     */
    public static List<VacationRequestModel> findOverlappingRequests(
            UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return list("employee.id = ?1 and isActive = true and " +
                        "((startDate <= ?3 and endDate >= ?2) or " +
                        "(startDate >= ?2 and startDate <= ?3))",
                employeeId, startDate, endDate);
    }

    /**
     * Counts total vacation days requested by an employee in a specific year.
     *
     * @param employeeId The UUID of the employee.
     * @param year The year to count vacation days for.
     * @return The total number of vacation days requested.
     */
    public static Long countVacationDaysByYear(UUID employeeId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        List<VacationRequestModel> requests = list(
                "employee.id = ?1 and vacationStatus = ?2 and " +
                        "startDate >= ?3 and endDate <= ?4",
                employeeId, VacationStatusEnum.APPROVED, startOfYear, endOfYear
        );

        return requests.stream()
                .mapToLong(r -> r.daysRequested != null ? r.daysRequested : 0L)
                .sum();
    }

    /**
     * Finds all vacation requests that need approval by a specific manager.
     * Assumes the manager manages the employees who submitted the requests.
     *
     * @param managerId The UUID of the manager.
     * @return A list of pending VacationRequestModel for the manager's subordinates.
     */
    public static List<VacationRequestModel> findPendingForManager(UUID managerId) {
        return list("employee.manager.id = ?1 and vacationStatus = ?2 and isActive = true",
                managerId, VacationStatusEnum.PENDING);
    }
}