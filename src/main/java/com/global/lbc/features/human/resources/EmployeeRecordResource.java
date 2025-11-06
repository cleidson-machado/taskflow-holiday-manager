package com.global.lbc.features.human.resources;

import com.global.lbc.features.human.resources.domain.FiscalNumber;
import com.global.lbc.features.human.resources.domain.SocialNumber;
import com.global.lbc.util.PaginatedResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * REST Resource for Employee management.
 * Provides CRUD operations and search capabilities for employees.
 *
 * Base path: /employees
 */
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeRecordResource {

    @Inject
    EmployeeService employeeService;

    // ========== LISTAGEM E PAGINAÇÃO ==========

    /**
     * Lists employees with optional pagination and sorting.
     *
     * Examples:
     * GET /employees - Returns first 50 active employees
     * GET /employees?page=0&size=20 - Paginated list
     * GET /employees?page=0&size=20&sort=name&order=asc - With sorting
     * GET /employees?sort=hireDate&order=desc - Last 50 by hire date
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortField Field to sort by (name, surname, hireDate, employeeRole, etc.)
     * @param sortOrder Sort order (asc/desc)
     * @return Response with employee list or paginated response
     */
    @GET
    public Response listEmployees(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("sort") @DefaultValue("name") String sortField,
            @QueryParam("order") @DefaultValue("asc") String sortOrder
    ) {
        try {
            if (page != null && size != null) {
                PaginatedResponse<EmployeeRecordModel> pagedResponse =
                        employeeService.getPaginatedEmployees(page, size, sortField, sortOrder);
                return Response.ok(pagedResponse).build();
            } else {
                List<EmployeeRecordModel> limitedResponse =
                        employeeService.getFirst50ActiveEmployees();
                return Response.ok(limitedResponse).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Lists only active employees with pagination.
     *
     * Example: GET /employees/active?page=0&size=20
     *
     * @param page Page number
     * @param size Page size
     * @return Paginated response with active employees
     */
    @GET
    @Path("/active")
    public Response listActiveEmployees(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        try {
            PaginatedResponse<EmployeeRecordModel> response =
                    employeeService.getActiveEmployees(page, size);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // ========== BUSCA POR ID ==========

    /**
     * Gets an employee by ID.
     *
     * Example: GET /employees/123e4567-e89b-12d3-a456-426614174000
     *
     * @param idStr Employee UUID as string
     * @return Response with employee data or error
     */
    @GET
    @Path("/{id}")
    public Response getEmployeeById(@PathParam("id") String idStr) {
        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            String errorMsg = "ID inválido: " + e.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new EmployeeResponse(null, errorMsg))
                    .build();
        }

        return employeeService.findById(id)
                .map(employee -> Response.ok()
                        .entity(new EmployeeResponse(employee, "Empregado encontrado com sucesso"))
                        .build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new EmployeeResponse(null, "Empregado não encontrado com ID: " + id))
                        .build());
    }

    // ========== BUSCA POR NIF (FISCAL NUMBER) ==========

    /**
     * Searches for an employee by fiscal number (NIF).
     *
     * Example: GET /employees/nif/123456789
     *
     * @param nifStr Fiscal number as string
     * @return Response with employee data or error
     */
    @GET
    @Path("/nif/{nif}")
    public Response findByFiscalNumber(@PathParam("nif") String nifStr) {
        try {
            FiscalNumber fiscalNumber = FiscalNumber.of(nifStr);
            EmployeeRecordModel employee = EmployeeRecordModel.findByFiscalNumber(fiscalNumber.getValue());

            if (employee == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Empregado não encontrado com NIF: " + nifStr))
                        .build();
            }

            return Response.ok(employee).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("NIF inválido: " + e.getMessage()))
                    .build();
        }
    }

    // ========== BUSCA POR NISS (SOCIAL NUMBER) ==========

    /**
     * Searches for an employee by social security number (NISS).
     *
     * Example: GET /employees/niss/12345678901
     *
     * @param nissStr Social number as string
     * @return Response with employee data or error
     */
    @GET
    @Path("/niss/{niss}")
    public Response findBySocialNumber(@PathParam("niss") String nissStr) {
        try {
            SocialNumber socialNumber = SocialNumber.of(nissStr);
            EmployeeRecordModel employee = EmployeeRecordModel.findBySocialNumber(socialNumber.getValue());

            if (employee == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Empregado não encontrado com NISS: " + nissStr))
                        .build();
            }

            return Response.ok(employee).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("NISS inválido: " + e.getMessage()))
                    .build();
        }
    }

    // ========== BUSCA POR NOME ==========

    /**
     * Searches employees by name or surname (partial match, case-insensitive).
     *
     * Example: GET /employees/search?q=João
     *
     * @param query Search term
     * @return Response with list of matching employees
     */
    @GET
    @Path("/search")
    public Response searchByName(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("O parâmetro de busca 'q' não pode ser vazio"))
                    .build();
        }

        try {
            List<EmployeeRecordModel> results = employeeService.searchByName(query);
            return Response.ok(results).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // ========== BUSCA POR CARGO ==========

    /**
     * Lists employees by role.
     *
     * Example: GET /employees/role/MANAGER
     *
     * @param roleStr Employee role
     * @return Response with list of employees
     */
    @GET
    @Path("/role/{role}")
    public Response findByRole(@PathParam("role") String roleStr) {
        try {
            EmployeeRoleEnum role = EmployeeRoleEnum.valueOf(roleStr.toUpperCase());
            List<EmployeeRecordModel> employees = employeeService.findByRole(role);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Cargo inválido: " + roleStr))
                    .build();
        }
    }

    // ========== BUSCA POR TIPO DE CONTRATO ==========

    /**
     * Lists employees by contract type.
     *
     * Example: GET /employees/contract/FULL_TIME
     *
     * @param contractStr Contract type
     * @return Response with list of employees
     */
    @GET
    @Path("/contract/{contract}")
    public Response findByContractType(@PathParam("contract") String contractStr) {
        try {
            TypeOfContractEnum contract = TypeOfContractEnum.valueOf(contractStr.toUpperCase());
            List<EmployeeRecordModel> employees = employeeService.findByContractType(contract);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Tipo de contrato inválido: " + contractStr))
                    .build();
        }
    }

    // ========== HIERARQUIA ==========

    /**
     * Gets all subordinates of a manager.
     *
     * Example: GET /employees/123e4567-e89b-12d3-a456-426614174000/subordinates
     *
     * @param idStr Manager UUID
     * @return Response with list of subordinates
     */
    @GET
    @Path("/{id}/subordinates")
    public Response getSubordinates(@PathParam("id") String idStr) {
        try {
            UUID managerId = UUID.fromString(idStr);
            List<EmployeeRecordModel> subordinates = employeeService.getSubordinates(managerId);
            return Response.ok(subordinates).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("ID inválido: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Lists all managers in the organization.
     *
     * Example: GET /employees/managers
     *
     * @return Response with list of managers
     */
    @GET
    @Path("/managers")
    public Response getAllManagers() {
        List<EmployeeRecordModel> managers = employeeService.getAllManagers();
        return Response.ok(managers).build();
    }

    /**
     * Lists top-level employees (without managers).
     *
     * Example: GET /employees/top-level
     *
     * @return Response with list of top-level employees
     */
    @GET
    @Path("/top-level")
    public Response getTopLevelEmployees() {
        List<EmployeeRecordModel> topLevel = employeeService.getTopLevelEmployees();
        return Response.ok(topLevel).build();
    }

    // ========== CRUD OPERATIONS ==========

    /**
     * Creates a new employee.
     *
     * Example: POST /employees
     * Body: { "name": "João", "surname": "Silva", ... }
     *
     * @param employee Employee data
     * @return Response with created employee
     */
    @POST
    @Transactional
    public Response create(@Valid EmployeeRecordModel employee) {
        try {
            EmployeeRecordModel created = employeeService.createEmployee(employee);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Updates an existing employee.
     *
     * Example: PUT /employees/123e4567-e89b-12d3-a456-426614174000
     * Body: { "name": "João", "surname": "Silva", ... }
     *
     * @param idStr Employee UUID
     * @param employee Updated employee data
     * @return Response with updated employee
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String idStr, @Valid EmployeeRecordModel employee) {
        try {
            UUID id = UUID.fromString(idStr);
            EmployeeRecordModel updated = employeeService.updateEmployee(id, employee);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Deactivates an employee (soft delete).
     *
     * Example: PATCH /employees/123e4567-e89b-12d3-a456-426614174000/deactivate
     *
     * @param idStr Employee UUID
     * @return Response with success message
     */
    @PATCH
    @Path("/{id}/deactivate")
    @Transactional
    public Response deactivate(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);
            employeeService.deactivateEmployee(id);
            return Response.ok()
                    .entity(new SuccessResponse("Empregado desativado com sucesso"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Permanently deletes an employee.
     *
     * Example: DELETE /employees/123e4567-e89b-12d3-a456-426614174000
     *
     * @param idStr Employee UUID
     * @return Response with no content or error
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);
            employeeService.deleteEmployee(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // ========== GESTÃO DE GERENTE ==========

    /**
     * Assigns a manager to an employee.
     *
     * Example: PUT /employees/123.../manager/456...
     *
     * @param employeeIdStr Employee UUID
     * @param managerIdStr Manager UUID
     * @return Response with success message
     */
    @PUT
    @Path("/{employeeId}/manager/{managerId}")
    @Transactional
    public Response assignManager(
            @PathParam("employeeId") String employeeIdStr,
            @PathParam("managerId") String managerIdStr
    ) {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            UUID managerId = UUID.fromString(managerIdStr);
            employeeService.assignManager(employeeId, managerId);
            return Response.ok()
                    .entity(new SuccessResponse("Gerente atribuído com sucesso"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Removes manager from an employee.
     *
     * Example: DELETE /employees/123.../manager
     *
     * @param employeeIdStr Employee UUID
     * @return Response with success message
     */
    @DELETE
    @Path("/{employeeId}/manager")
    @Transactional
    public Response removeManager(@PathParam("employeeId") String employeeIdStr) {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            employeeService.removeManager(employeeId);
            return Response.ok()
                    .entity(new SuccessResponse("Gerente removido com sucesso"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // ========== ESTATÍSTICAS ==========

    /**
     * Gets statistics about employees.
     *
     * Example: GET /employees/stats
     *
     * @return Response with employee statistics
     */
    @GET
    @Path("/stats")
    public Response getStatistics() {
        long totalActive = employeeService.countActiveEmployees();
        return Response.ok()
                .entity(new StatsResponse(totalActive))
                .build();
    }

    // ========== RESPONSE RECORDS ==========

    /**
     * Response wrapper for employee with message.
     */
    public record EmployeeResponse(EmployeeRecordModel employee, String message) {}

    /**
     * Error response wrapper.
     */
    public record ErrorResponse(String error) {}

    /**
     * Success response wrapper.
     */
    public record SuccessResponse(String message) {}

    /**
     * Statistics response.
     */
    public record StatsResponse(long totalActiveEmployees) {}
}