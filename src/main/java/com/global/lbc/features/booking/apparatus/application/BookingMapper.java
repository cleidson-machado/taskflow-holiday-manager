package com.global.lbc.features.booking.apparatus.application;

import com.global.lbc.features.booking.apparatus.application.dto.BookingRequest;
import com.global.lbc.features.booking.apparatus.application.dto.BookingResponse;
import com.global.lbc.features.booking.apparatus.model.Booking;
import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.vacation.apparatus.application.dto.EmployeeResponseSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BookingMapper {

    // =======================================================
    // 1. Mapeamento para DTO (RESPONSE)
    // =======================================================

    public BookingResponse toResponse(Booking entity) {
        if (entity == null) {
            return null;
        }

        BookingResponse response = new BookingResponse();

        // Mapeamento dos campos simples
        response.id = entity.id;
        response.vacationId = entity.vacationId;
        response.startDate = entity.startDate;
        response.endDate = entity.endDate;
        response.daysReserved = entity.daysReserved;
        response.bookingStatus = entity.bookingStatus;
        response.isActive = entity.isActive;
        response.requestNotes = entity.requestNotes;

        // Mapeamento dos campos private (via getter)
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();

        // Mapeamento do Soft Delete
        response.deletedAt = entity.deletedAt;
        response.deletedBy = entity.deletedBy;

        // Mapeamento do Objeto Aninhado (o "JSON Rico")
        if (entity.employee != null) {
            response.employee = mapEmployeeToSummary(entity.employee);
        }

        return response;
    }

    private EmployeeResponseSummary mapEmployeeToSummary(Employee employee) {
        // Assume que a entidade Employee tem os campos públicos para acesso
        return new EmployeeResponseSummary(
                employee.id,
                employee.name,
                employee.surname,
                employee.fiscalNumber
        );
    }

    // =======================================================
    // 2. Mapeamento para Entidade (REQUEST - Criação)
    // =======================================================

    public Booking toEntity(BookingRequest dto) {
        Booking entity = new Booking();

        // Busca a entidade Employee para o relacionamento (Foreign Key)
        if (dto.employeeId != null) {
            Employee employee = Employee.findById(dto.employeeId);
            if (employee == null) {
                // Devemos garantir que apenas IDs válidos sejam persistidos
                throw new NotFoundException("Employee not found with ID: " + dto.employeeId);
            }
            entity.employee = employee;
        } else {
            throw new IllegalArgumentException("Employee ID is required for a new booking.");
        }

        entity.startDate = dto.startDate;
        entity.endDate = dto.endDate;
        entity.requestNotes = dto.requestNotes;
        // Campos de status/dias são definidos pelo Service/Entidade

        return entity;
    }

    // =======================================================
    // 3. Mapeamento para Atualização da Entidade (UPDATE)
    // =======================================================

    public void updateEntity(Booking entity, BookingRequest dto) {
        // Atualiza campos que fazem sentido serem alterados via UPDATE Request

        // Se o Employee ID for passado e for diferente do atual, atualiza o relacionamento
        if (dto.employeeId != null && !dto.employeeId.equals(entity.employee.id)) {
            Employee newEmployee = Employee.findById(dto.employeeId);
            if (newEmployee == null) {
                throw new NotFoundException("Employee not found with ID: " + dto.employeeId);
            }
            entity.employee = newEmployee;
        }

        // Atualiza datas
        entity.startDate = dto.startDate;
        entity.endDate = dto.endDate;

        // Atualiza notas
        entity.requestNotes = dto.requestNotes;

        // Nota: O Service é responsável pelo recálculo de daysReserved se as datas mudarem.
    }
}

