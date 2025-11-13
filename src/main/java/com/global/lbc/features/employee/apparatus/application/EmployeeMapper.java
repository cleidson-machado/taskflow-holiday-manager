package com.global.lbc.features.employee.apparatus.application;

import com.global.lbc.features.employee.apparatus.application.dto.EmployeeResponse;
import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.factory.TaxIdentifierFactory;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxIdentifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class EmployeeMapper {

    // =======================================================
    // 1. Mapeamento para DTO (RESPONSE)
    // =======================================================

    public EmployeeResponse toResponse(Employee entity) {
        if (entity == null) {
            return null;
        }

        EmployeeResponse dto = new EmployeeResponse();

        // Mapeamento dos campos simples
        dto.id = entity.id;
        dto.name = entity.name;
        dto.surname = entity.surname;
        dto.fiscalNumber = entity.fiscalNumber;
        dto.fiscalNumberCountry = entity.fiscalNumberCountry;
        dto.socialNumber = entity.socialNumber;
        dto.dateOfBirth = entity.dateOfBirth;
        dto.employmentType = entity.employmentType;
        dto.employeeRole = entity.employeeRole;
        dto.hireDate = entity.hireDate;
        dto.terminationDate = entity.terminationDate;
        dto.salaryBase = entity.salaryBase;
        dto.isActive = entity.isActive;
        dto.managerId = entity.manager != null ? entity.manager.id : null;
        dto.vacationDaysBalance = entity.vacationDaysBalance;
        dto.vacationDaysUsed = entity.vacationDaysUsed;

        // Mapeamento dos campos private (via getter)
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();

        return dto;
    }

    // =======================================================
    // 2. Mapeamento para Entidade (REQUEST - Criação)
    // =======================================================

    public Employee toEntity(EmployeeResponse dto) {
        Employee entity = new Employee();

        entity.name = dto.name;
        entity.surname = dto.surname;
        entity.dateOfBirth = dto.dateOfBirth;
        entity.employmentType = dto.employmentType;
        entity.employeeRole = dto.employeeRole;
        entity.hireDate = dto.hireDate;
        entity.terminationDate = dto.terminationDate;
        entity.salaryBase = dto.salaryBase;
        entity.isActive = dto.isActive != null ? dto.isActive : true;
        entity.vacationDaysBalance = dto.vacationDaysBalance != null ? dto.vacationDaysBalance : 0L;
        entity.vacationDaysUsed = dto.vacationDaysUsed != null ? dto.vacationDaysUsed : 0L;

        // Configuração do Tax Identifier
        if (dto.fiscalNumber != null && dto.fiscalNumberCountry != null) {
            TaxIdentifier taxId = TaxIdentifierFactory.create(dto.fiscalNumberCountry, dto.fiscalNumber);
            entity.setTaxIdentifier(taxId);
        }

        // Configuração do Social Number
        if (dto.socialNumber != null) {
            entity.socialNumber = dto.socialNumber;
        }

        // Configuração do relacionamento com Manager
        if (dto.managerId != null) {
            Employee manager = Employee.findById(dto.managerId);
            if (manager != null) {
                entity.manager = manager;
            }
        }

        return entity;
    }

    // =======================================================
    // 3. Mapeamento para Atualização da Entidade (UPDATE)
    // =======================================================

    public void updateEntity(Employee entity, EmployeeResponse dto) {
        // Atualiza campos que fazem sentido serem alterados via UPDATE Request
        entity.name = dto.name;
        entity.surname = dto.surname;
        entity.dateOfBirth = dto.dateOfBirth;
        entity.employmentType = dto.employmentType;
        entity.employeeRole = dto.employeeRole;
        entity.hireDate = dto.hireDate;
        entity.terminationDate = dto.terminationDate;
        entity.salaryBase = dto.salaryBase;
        entity.isActive = dto.isActive;
        entity.vacationDaysBalance = dto.vacationDaysBalance;
        entity.vacationDaysUsed = dto.vacationDaysUsed;

        // Atualiza Tax Identifier se fornecido
        if (dto.fiscalNumber != null && dto.fiscalNumberCountry != null) {
            TaxIdentifier taxId = TaxIdentifierFactory.create(dto.fiscalNumberCountry, dto.fiscalNumber);
            entity.setTaxIdentifier(taxId);
        }

        // Atualiza Social Number se fornecido
        if (dto.socialNumber != null) {
            entity.socialNumber = dto.socialNumber;
        }

        // Atualiza relacionamento com Manager
        if (dto.managerId != null) {
            Employee manager = Employee.findById(dto.managerId);
            if (manager == null) {
                throw new NotFoundException("Manager not found with ID: " + dto.managerId);
            }
            entity.manager = manager;
        } else {
            entity.manager = null;
        }
    }
}
