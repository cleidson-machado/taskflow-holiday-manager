package com.global.lbc.features.booking.apparatus.model;

import com.global.lbc.features.booking.apparatus.model.util.BookingStatus;
import com.global.lbc.features.employee.apparatus.model.Employee;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// ============================================================
// ENTITY: Booking (Reserva de Agenda para Férias)
// Esta entidade serve como um "bloqueador de agenda" (Limitador).
// Ela garante que as datas de férias não se sobreponham entre colaboradores.
// Um registro de Vacation só pode ser criado a partir de um Booking aprovado.
// ============================================================

@Entity
@Table(name = "vacation_booking")
public class Booking extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // Colaborador que fez a reserva
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_employee"))
    public Employee employee;

    // Referência opcional para o pedido de férias final
    // Será preenchido quando a Vacation for criada
    @Column(name = "vacation_id")
    public UUID vacationId;

    // ========== PERÍODO RESERVADO ==========
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    @Column(name = "days_reserved", nullable = false)
    public Integer daysReserved;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    public Boolean isActive = true;

    // ========== OBSERVAÇÕES ==========
    @Column(name = "request_notes", columnDefinition = "TEXT")
    public String requestNotes;

    // ========== STATUS DA RESERVA ==========
    @Column(name = "booking_status", nullable = false)
    @Enumerated(EnumType.STRING)
    public BookingStatus bookingStatus = BookingStatus.RESERVED;

    // ========== SOFT DELETE (Flag e Auditoria) ==========
    // soft delete fields ------------------------ start
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    public String deletedBy;
    // soft delete fields ------------------------ end


    // ========== AUDITORIA ==========
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

    // GETTERS SOMENTE PARA OS CAMPOS PRIVATE (Auditoria)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Métodos de conveniência
    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}