package com.global.lbc.features.employee.apparatus.application.service;

import com.global.lbc.features.employee.apparatus.application.EmployeeMapper;
import com.global.lbc.features.employee.apparatus.application.dto.EmployeeResponse;
import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.factory.TaxIdentifierFactory;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxIdentifier;
import com.global.lbc.features.employee.apparatus.model.util.EmployeeRole;
import com.global.lbc.features.employee.apparatus.model.util.EmploymentType;
import com.global.lbc.features.employee.apparatus.usecases.pt.social.number.SocialNumber;
import com.global.lbc.shared.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeService {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int PURGE_DAYS_THRESHOLD = 30;

    @Inject
    EmployeeMapper mapper;

    public PaginatedResponse<EmployeeResponse> getPaginatedEmployees(int page, int size, String sortField, String sortOrder) {
        validatePagination(page, size);
        validateSortField(sortField);

        Sort sortBy = buildSort(sortField, sortOrder);

        var query = Employee.<Employee>findAll(sortBy).page(page, size);
        long totalItems = Employee.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<EmployeeResponse> employees = query.list().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                employees,
                totalItems,
                totalPages,
                page
        );
    }

    public List<EmployeeResponse> getFirst50ActiveEmployees() {
        return Employee.<Employee>find("isActive = true", Sort.by("name").ascending())
                .page(0, DEFAULT_PAGE_SIZE)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<EmployeeResponse> getActiveEmployees(int page, int size) {
        validatePagination(page, size);

        var query = Employee.<Employee>find("isActive = true", Sort.by("name").ascending())
                .page(page, size);
        long totalItems = Employee.count("isActive = true");
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<EmployeeResponse> employees = query.list().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                employees,
                totalItems,
                totalPages,
                page
        );
    }

    public Optional<EmployeeResponse> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Employee employee = Employee.findById(id);
        return Optional.ofNullable(employee).map(mapper::toResponse);
    }

    public EmployeeResponse findByFiscalNumber(String fiscalNumber, String country) {
        if (fiscalNumber == null || fiscalNumber.isBlank()) {
            throw new IllegalArgumentException("Fiscal number cannot be empty");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be empty");
        }

        Employee employee = Employee.find(
                "fiscalNumber = ?1 AND fiscalNumberCountry = ?2 AND isActive = true",
                fiscalNumber, country
        ).firstResult();

        if (employee == null) {
            throw new NotFoundException("Employee not found with fiscal number: " + fiscalNumber);
        }

        return mapper.toResponse(employee);
    }

    public EmployeeResponse findBySocialNumber(String socialNumberStr) {
        if (socialNumberStr == null || socialNumberStr.isBlank()) {
            throw new IllegalArgumentException("Social number cannot be empty");
        }

        SocialNumber socialNumber = SocialNumber.of(socialNumberStr);
        Employee employee = Employee.find(
                "socialNumber = ?1 AND isActive = true",
                socialNumber
        ).firstResult();

        if (employee == null) {
            throw new NotFoundException("Employee not found with social number: " + socialNumberStr);
        }

        return mapper.toResponse(employee);
    }

    public List<EmployeeResponse> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }

        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return Employee.<Employee>find(
                        "LOWER(name) LIKE ?1 OR LOWER(surname) LIKE ?1",
                        pattern
                )
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> findByRole(EmployeeRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        return Employee.<Employee>find("employeeRole = ?1 AND isActive = true", role)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> findByEmploymentType(EmploymentType type) {
        if (type == null) {
            throw new IllegalArgumentException("Employment type cannot be null");
        }

        return Employee.<Employee>find("employmentType = ?1 AND isActive = true", type)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean existsByFiscalNumber(String fiscalNumber, String country) {
        if (fiscalNumber == null || country == null) {
            return false;
        }
        return Employee.count("fiscalNumber = ?1 AND fiscalNumberCountry = ?2", fiscalNumber, country) > 0;
    }

    public boolean existsByFiscalNumberExcludingId(String fiscalNumber, String country, UUID excludeId) {
        if (fiscalNumber == null || country == null) {
            return false;
        }
        return Employee.count(
                "fiscalNumber = ?1 AND fiscalNumberCountry = ?2 AND id != ?3",
                fiscalNumber, country, excludeId
        ) > 0;
    }

    public boolean existsBySocialNumber(SocialNumber socialNumber) {
        if (socialNumber == null) {
            return false;
        }
        return Employee.count("socialNumber = ?1", socialNumber) > 0;
    }

    public boolean existsBySocialNumberExcludingId(SocialNumber socialNumber, UUID excludeId) {
        if (socialNumber == null) {
            return false;
        }
        return Employee.count("socialNumber = ?1 AND id != ?2", socialNumber, excludeId) > 0;
    }

    public void validateUniqueIdentifiers(String fiscalNumber, String country, String socialNumber, UUID excludeId) {
        if (fiscalNumber != null && country != null) {
            boolean fiscalExists = (excludeId == null)
                    ? existsByFiscalNumber(fiscalNumber, country)
                    : existsByFiscalNumberExcludingId(fiscalNumber, country, excludeId);

            if (fiscalExists) {
                throw new IllegalArgumentException("Fiscal number already registered: " + fiscalNumber);
            }
        }

        if (socialNumber != null) {
            SocialNumber social = SocialNumber.of(socialNumber);
            boolean socialExists = (excludeId == null)
                    ? existsBySocialNumber(social)
                    : existsBySocialNumberExcludingId(social, excludeId);

            if (socialExists) {
                throw new IllegalArgumentException("Social number already registered: " + socialNumber);
            }
        }
    }

    @Transactional
    public EmployeeResponse create(EmployeeResponse dto) {
        validateEmployeeData(dto);

        String socialNumberStr = (dto.socialNumber != null) ? dto.socialNumber.getValue() : null;
        validateUniqueIdentifiers(dto.fiscalNumber, dto.fiscalNumberCountry, socialNumberStr, null);

        Employee employee = mapper.toEntity(dto);

        if (employee.manager != null) {
            validateManagerAssignment(null, employee.manager.id);
        }

        employee.persist();

        return mapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeResponse dto) {
        Employee employee = Employee.findById(id);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }

        validateEmployeeData(dto);

        String socialNumberStr = (dto.socialNumber != null) ? dto.socialNumber.getValue() : null;
        validateUniqueIdentifiers(dto.fiscalNumber, dto.fiscalNumberCountry, socialNumberStr, id);

        mapper.updateEntity(employee, dto);

        if (employee.manager != null) {
            validateManagerAssignment(id, employee.manager.id);
        }

        return mapper.toResponse(employee);
    }

    @Transactional
    public void deactivateEmployee(UUID id, String deletedBy) {
        Employee employee = Employee.findById(id);

        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }

        if (Boolean.FALSE.equals(employee.isActive) && employee.deletedAt != null) {
            throw new BadRequestException("Employee already deleted");
        }

        if (hasActiveSubordinates(id)) {
            throw new BadRequestException(
                    "Cannot delete manager with active subordinates. " +
                            "Reassign subordinates first."
            );
        }

        String userIdentifier = (deletedBy != null && !deletedBy.isBlank())
                ? deletedBy
                : "system";

        employee.isActive = false;
        employee.deletedAt = LocalDateTime.now();
        employee.deletedBy = userIdentifier;
        employee.terminationDate = LocalDate.now();

        employee.persist();
    }

    @Transactional
    public void deactivateEmployee(UUID id) {
        deactivateEmployee(id, "system");
    }

    @Transactional
    public void delete(UUID id) {
        deactivateEmployee(id, "system");
    }

    @Transactional
    public void restoreEmployee(UUID id) {
        Employee employee = findByIdIncludingDeleted(id);

        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }

        if (Boolean.TRUE.equals(employee.isActive) || employee.deletedAt == null) {
            throw new BadRequestException("Employee is not deleted");
        }

        employee.isActive = true;
        employee.deletedAt = null;
        employee.deletedBy = null;
        employee.terminationDate = null;

        employee.persist();
    }

    @Transactional
    public void purgeEmployee(UUID id) {
        Employee employee = findByIdIncludingDeleted(id);

        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }

        if (Boolean.TRUE.equals(employee.isActive) || employee.deletedAt == null) {
            throw new BadRequestException(
                    "Only soft-deleted records can be purged. " +
                            "Execute soft delete first."
            );
        }

        if (employee.deletedAt.isAfter(LocalDateTime.now().minusDays(PURGE_DAYS_THRESHOLD))) {
            throw new BadRequestException(
                    String.format(
                            "Only records deleted for more than %d days can be purged. " +
                                    "Deletion date: %s",
                            PURGE_DAYS_THRESHOLD,
                            employee.deletedAt.toString()
                    )
            );
        }

        long totalSubordinates = Employee.count("manager.id = ?1", id);
        if (totalSubordinates > 0) {
            throw new BadRequestException(
                    "Cannot purge employee with subordinates in history. " +
                            "Reassign subordinates first."
            );
        }

        employee.delete();
    }

    public List<EmployeeResponse> getDeletedEmployees() {
        return Employee.<Employee>find("isActive = false AND deletedAt IS NOT NULL")
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<EmployeeResponse> getDeletedEmployees(int page, int size) {
        validatePagination(page, size);

        var query = Employee.<Employee>find("isActive = false AND deletedAt IS NOT NULL", Sort.by("deletedAt").descending())
                .page(page, size);
        long totalItems = Employee.count("isActive = false AND deletedAt IS NOT NULL");
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<EmployeeResponse> employees = query.list().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                employees,
                totalItems,
                totalPages,
                page
        );
    }

    public List<EmployeeResponse> getDeletedEmployeesBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return Employee.<Employee>find(
                        "isActive = false AND deletedAt BETWEEN ?1 AND ?2",
                        startDate, endDate
                )
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getDeletedEmployeesByUser(String deletedBy) {
        if (deletedBy == null || deletedBy.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        return Employee.<Employee>find("isActive = false AND deletedBy = ?1", deletedBy)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignManager(UUID employeeId, UUID managerId) {
        validateManagerAssignment(employeeId, managerId);

        Employee employee = Employee.findById(employeeId);
        Employee manager = Employee.findById(managerId);

        if (employee == null || manager == null) {
            throw new NotFoundException("Employee or manager not found");
        }

        employee.manager = manager;
    }

    @Transactional
    public void removeManager(UUID employeeId) {
        Employee employee = Employee.findById(employeeId);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + employeeId);
        }
        employee.manager = null;
    }

    public List<EmployeeResponse> getSubordinates(UUID managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("Manager ID cannot be null");
        }

        return Employee.<Employee>find("manager.id = ?1 AND isActive = true", managerId)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getAllManagers() {
        List<UUID> managerIds = Employee.find(
                "SELECT DISTINCT e.manager.id FROM Employee e WHERE e.manager IS NOT NULL AND e.isActive = true"
        ).project(UUID.class).list();

        if (managerIds.isEmpty()) {
            return List.of();
        }

        return Employee.<Employee>find("id IN ?1 AND isActive = true", managerIds)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getTopLevelEmployees() {
        return Employee.<Employee>find("manager IS NULL AND isActive = true")
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> findAll(int page, int size) {
        validatePagination(page, size);

        return Employee.<Employee>findAll(Sort.by("name").ascending())
                .page(page, size)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> findActiveEmployees(int page, int size) {
        validatePagination(page, size);

        return Employee.<Employee>find("isActive = true", Sort.by("name").ascending())
                .page(page, size)
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateEmployeeData(EmployeeResponse dto) {
        if (dto.name == null || dto.name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (dto.surname == null || dto.surname.isBlank()) {
            throw new IllegalArgumentException("Surname is required");
        }

        if (dto.employmentType == null) {
            throw new IllegalArgumentException("Employment type is required");
        }

        if (dto.employeeRole == null) {
            throw new IllegalArgumentException("Employee role is required");
        }

        if (dto.hireDate == null) {
            throw new IllegalArgumentException("Hire date is required");
        }

        if (dto.hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hire date cannot be in the future");
        }

        if (dto.dateOfBirth != null && dto.dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        if (dto.terminationDate != null && dto.terminationDate.isBefore(dto.hireDate)) {
            throw new IllegalArgumentException("Termination date cannot be before hire date");
        }

        if (dto.salaryBase != null && dto.salaryBase < 0) {
            throw new IllegalArgumentException("Salary base cannot be negative");
        }
    }

    private void validateManagerAssignment(UUID employeeId, UUID managerId) {
        if (managerId == null) {
            return;
        }

        if (employeeId != null && employeeId.equals(managerId)) {
            throw new BadRequestException("Employee cannot be their own manager");
        }

        if (employeeId != null && wouldCreateCircularReference(employeeId, managerId)) {
            throw new BadRequestException("Assignment would create circular reference");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void validateSortField(String field) {
        if (!isValidSortField(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }
    }

    private boolean isValidSortField(String field) {
        return List.of(
                "name",
                "surname",
                "hireDate",
                "employeeRole",
                "employmentType",
                "isActive",
                "createdAt",
                "dateOfBirth",
                "salaryBase",
                "deletedAt"
        ).contains(field);
    }

    private Sort buildSort(String sortField, String sortOrder) {
        return "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
    }

    private boolean hasActiveSubordinates(UUID managerId) {
        return Employee.count("manager.id = ?1 AND isActive = true", managerId) > 0;
    }

    private boolean wouldCreateCircularReference(UUID employeeId, UUID managerId) {
        Employee currentManager = Employee.findById(managerId);

        while (currentManager != null) {
            if (currentManager.id.equals(employeeId)) {
                return true;
            }
            currentManager = currentManager.manager;
        }

        return false;
    }

    private Employee findByIdIncludingDeleted(UUID id) {
        return Employee.findById(id);
    }

    public long countActiveEmployees() {
        return Employee.count("isActive = true");
    }

    public long countDeletedEmployees() {
        return Employee.count("isActive = false AND deletedAt IS NOT NULL");
    }

    public long countByRole(EmployeeRole role) {
        return Employee.count("employeeRole = ?1 AND isActive = true", role);
    }

    public long countByEmploymentType(EmploymentType type) {
        return Employee.count("employmentType = ?1 AND isActive = true", type);
    }

    public List<EmployeeResponse> findByHireDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return Employee.<Employee>find(
                        "hireDate >= ?1 AND hireDate <= ?2 AND isActive = true",
                        startDate, endDate
                )
                .list()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}