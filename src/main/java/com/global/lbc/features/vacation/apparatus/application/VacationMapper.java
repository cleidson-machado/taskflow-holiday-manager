package com.global.lbc.features.vacation.apparatus.application;

import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.vacation.apparatus.application.dto.EmployeeResponseSummary;
import com.global.lbc.features.vacation.apparatus.application.dto.VacationRequest; // Added for input conversion
import com.global.lbc.features.vacation.apparatus.application.dto.VacationResponse;
import com.global.lbc.features.vacation.apparatus.model.Vacation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException; // To throw error if Employee is not found

/*
 * NOTE ON ARCHITECTURAL CHOICE: FOREIGN KEY VALIDATION (Employee lookup)
 *
 * This Mapper technically deviates from the strict Single Responsibility Principle (SRP)
 * by performing a database lookup (Employee.findById) and enforcing a structural
 * business rule (Employee must exist).
 *
 * The choice is made for two main pragmatic reasons:
 * 1. Fail-Fast Principle: It ensures the application throws a clean, high-level
 * 'NotFoundException' (404/400) immediately upon invalid input, rather than
 * deferring the check to the Service Layer or relying on a low-level database
 * Constraint Violation Exception when trying to persist the entity.
 * 2. Object Construction Necessity: The Vacation entity requires a fully loaded
 * Employee object (not just the ID) to establish its relationship. The Mapper,
 * responsible for building the valid Entity from the DTO, must handle this
 * conversion (ID -> Entity) and validate the ID's existence concurrently.
 *
 * For simpler, structural integrity checks (like Foreign Key existence), this approach
 * prioritizes code conciseness and clear error reporting over pure architectural layering.
 * Complex business rules (e.g., checking vacation balance) should remain in the Service Layer.
 */

@ApplicationScoped
public class VacationMapper {

    // =======================================================
    // 1. Mapping to DTO (RESPONSE)
    // =======================================================

    public VacationResponse toResponse(Vacation entity) {
        if (entity == null) {
            return null;
        }

        VacationResponse response = new VacationResponse();

        // Mapping simple fields
        response.id = entity.id;
        response.startDate = entity.startDate;
        response.endDate = entity.endDate;
        response.daysRequested = entity.daysRequested;
        response.vacationStatus = entity.vacationStatus;
        response.isActive = entity.isActive;
        response.approvingBy = entity.approvingBy;
        response.approvalDate = entity.approvalDate;
        response.requestNotes = entity.requestNotes;
        response.rejectionReason = entity.rejectionReason;

        // Mapping private fields (via getter)
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();

        // Mapping Soft Delete fields
        response.deletedAt = entity.deletedAt;
        response.deletedBy = entity.deletedBy;

        // Mapping Nested Object (the "Rich JSON")
        if (entity.employee != null) {
            response.employee = mapEmployeeToSummary(entity.employee);
        }

        return response;
    }

    private EmployeeResponseSummary mapEmployeeToSummary(Employee employee) {
        // Assumes the Employee entity has public fields for access
        return new EmployeeResponseSummary(
                employee.id,
                employee.name,
                employee.surname,
                employee.fiscalNumber
        );
    }

    // =======================================================
    // 2. Mapping to Entity (REQUEST - Creation)
    // =======================================================

    public Vacation toEntity(VacationRequest dto) {
        Vacation entity = new Vacation();

        // Fetches the Employee entity for the relationship (Foreign Key)
        if (dto.employeeId != null) {
            Employee employee = Employee.findById(dto.employeeId);
            if (employee == null) {
                // We must ensure that only valid IDs are persisted
                throw new NotFoundException("Employee not found with ID: " + dto.employeeId);
            }
            entity.employee = employee;
        } else {
            throw new IllegalArgumentException("Employee ID is required for a new vacation request.");
        }

        entity.startDate = dto.startDate;
        entity.endDate = dto.endDate;
        entity.requestNotes = dto.requestNotes;
        // Status/days fields are set by Service/Entity

        return entity;
    }

    // =======================================================
    // 3. Mapping for Entity Update ('UPDATE')
    // =======================================================

    public void updateEntity(Vacation entity, VacationRequest dto) {
        // Updates fields that make sense to be changed via 'UPDATE' Request

        // If Employee 'ID' is passed and is different from current, updates the relationship
        if (dto.employeeId != null && !dto.employeeId.equals(entity.employee.id)) {
            Employee newEmployee = Employee.findById(dto.employeeId);
            if (newEmployee == null) {
                throw new NotFoundException("Employee not found with ID: " + dto.employeeId);
            }
            entity.employee = newEmployee;
        }

        // Updates dates
        entity.startDate = dto.startDate;
        entity.endDate = dto.endDate;

        // Updates notes
        entity.requestNotes = dto.requestNotes;

        // Note: The Service is responsible for recalculating daysRequested if dates change.
    }
}