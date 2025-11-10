package com.global.lbc.features.vacation.apparatus.application.service;

import com.global.lbc.features.vacation.apparatus.application.VacationMapper;
import com.global.lbc.features.vacation.apparatus.application.dto.VacationRequest; // DTO para entrada de dados (Creation/Update)
import com.global.lbc.features.vacation.apparatus.application.dto.VacationResponse; // DTO para saída de dados
import com.global.lbc.features.vacation.apparatus.model.Vacation;
import com.global.lbc.features.vacation.apparatus.model.util.VacationStatus;
import com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates.VacationDaysBtCalculator;
import com.global.lbc.shared.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class VacationService {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    VacationDaysBtCalculator calculator;

    @Inject
    VacationMapper mapper;

    // --- MÉTODOS DE BUSCA (READ) ---

    public Optional<VacationResponse> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Vacation vacation = Vacation.findById(id);

        // Mapeia a Entidade para DTO
        return Optional.ofNullable(vacation).map(mapper::toResponse);
    }

    public PaginatedResponse<VacationResponse> getPaginatedVacations(int page, int size, String sortField, String sortOrder) {
        validatePagination(page, size);

        // A EmployeeService tinha validação de sortField, vou simplificar aqui, mas o ideal é validar.
        Sort sortBy = buildSort(sortField, sortOrder);

        var query = Vacation.<Vacation>findAll(sortBy).page(page, size);
        long totalItems = Vacation.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<VacationResponse> vacations = query.list().stream()
                .map(mapper::toResponse) // Usa o Mapper para converter
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                vacations,
                totalItems,
                totalPages,
                page
        );
    }

    // --- MÉTODOS DE ESCRITA (CREATE & UPDATE) ---

    @Transactional
    public VacationResponse create(VacationRequest dto) {
        // Validações de negócio do DTO (datas, funcionário existe, etc.)
        validateVacationData(dto);

        // 1. Mapeia DTO de Request para Entidade
        Vacation request = mapper.toEntity(dto);

        // 2. Lógica de Negócio: Cálculo de dias
        if (request.startDate != null && request.endDate != null) {
            request.daysRequested = calculator.calculateBusinessDays(
                    request.startDate,
                    request.endDate
            );
        }

        // 3. Persistência
        request.persist();

        // 4. Mapeia Entidade de volta para DTO de Response
        return mapper.toResponse(request);
    }

    @Transactional
    public VacationResponse update(UUID id, VacationRequest dto) {
        Vacation vacation = Vacation.findById(id);
        if (vacation == null) {
            throw new NotFoundException("Vacation request not found: " + id);
        }

        // Validações de negócio
        validateVacationData(dto);

        // 1. Mapeia DTO de Request para atualizar a Entidade
        mapper.updateEntity(vacation, dto);

        // 2. Lógica de Negócio: Recálculo de dias se as datas mudaram
        if (vacation.startDate != null && vacation.endDate != null) {
            vacation.daysRequested = calculator.calculateBusinessDays(
                    vacation.startDate,
                    vacation.endDate
            );
        }

        // Persistência (Panache faz o update no fim do @Transactional)
        return mapper.toResponse(vacation);
    }

    // --- MÉTODOS DE TRANSIÇÃO DE ESTADO ---

    @Transactional
    public VacationResponse approve(UUID vacationId, String approverName) {
        Vacation vacation = Vacation.findById(vacationId);
        if (vacation == null) {
            throw new NotFoundException("Vacation request not found.");
        }

        if (vacation.vacationStatus != VacationStatus.PENDING) {
            throw new IllegalStateException("The request is not in PENDING status.");
        }

        // Lógica de Transição de Estado
        vacation.vacationStatus = VacationStatus.APPROVED;
        vacation.approvingBy = approverName;
        vacation.approvalDate = LocalDateTime.now();
        vacation.rejectionReason = null;

        // Retorna DTO
        return mapper.toResponse(vacation);
    }

    @Transactional
    public VacationResponse reject(UUID vacationId, String approverName, String reason) {
        Vacation vacation = Vacation.findById(vacationId);
        if (vacation == null) {
            throw new NotFoundException("Vacation request not found.");
        }

        // Lógica de Transição de Estado
        vacation.vacationStatus = VacationStatus.REJECTED;
        vacation.approvingBy = approverName;
        vacation.approvalDate = LocalDateTime.now();
        vacation.rejectionReason = reason;

        // Retorna DTO
        return mapper.toResponse(vacation);
    }

    // --- MÉTODOS AUXILIARES (Padrão EmployeeService) ---

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void validateVacationData(VacationRequest dto) {
        if (dto.startDate == null || dto.endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        if (dto.startDate.isAfter(dto.endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        // Validações adicionais (ex: employeeId deve existir)
    }

    private Sort buildSort(String sortField, String sortOrder) {
        String field = (sortField == null || sortField.isBlank()) ? "startDate" : sortField;

        return "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}