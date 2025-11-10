package com.global.lbc.features.vacation.apparatus.application;

import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.vacation.apparatus.application.dto.EmployeeResponseSummary;
import com.global.lbc.features.vacation.apparatus.application.dto.VacationRequest; // Adicionado para conversão de entrada
import com.global.lbc.features.vacation.apparatus.application.dto.VacationResponse;
import com.global.lbc.features.vacation.apparatus.model.Vacation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException; // Para lançar erro se o Employee não for encontrado

@ApplicationScoped
public class VacationMapper {

    // =======================================================
    // 1. Mapeamento para DTO (RESPONSE)
    // =======================================================

    public VacationResponse toResponse(Vacation entity) {
        if (entity == null) {
            return null;
        }

        VacationResponse response = new VacationResponse();

        // Mapeamento dos campos simples
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

    public Vacation toEntity(VacationRequest dto) {
        Vacation entity = new Vacation();

        // Busca a entidade Employee para o relacionamento (Foreign Key)
        if (dto.employeeId != null) {
            Employee employee = Employee.findById(dto.employeeId);
            if (employee == null) {
                // Devemos garantir que apenas IDs válidos sejam persistidos
                throw new NotFoundException("Employee not found with ID: " + dto.employeeId);
            }
            entity.employee = employee;
        } else {
            throw new IllegalArgumentException("Employee ID is required for a new vacation request.");
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

    public void updateEntity(Vacation entity, VacationRequest dto) {
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

        // Nota: O Service é responsável pelo recálculo de daysRequested se as datas mudarem.
    }
}