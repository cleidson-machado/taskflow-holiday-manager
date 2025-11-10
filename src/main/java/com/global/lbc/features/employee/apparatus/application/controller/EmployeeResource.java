package com.global.lbc.features.employee.apparatus.application.controller;

import com.global.lbc.features.employee.apparatus.application.dto.EmployeeResponse;
import com.global.lbc.features.employee.apparatus.application.service.EmployeeService;
import com.global.lbc.features.employee.apparatus.model.util.EmployeeRole;
import com.global.lbc.features.employee.apparatus.model.util.EmploymentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    @Inject
    EmployeeService employeeService;

    @GET
    public Response listEmployees(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        try {
            List<EmployeeResponse> employees = employeeService.findAll(page, size);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/active")
    public Response listActiveEmployees(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        try {
            List<EmployeeResponse> employees = employeeService.findActiveEmployees(page, size);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getEmployeeById(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);
            // ✅ CORREÇÃO: Desempacotar o Optional
            return employeeService.findById(id)
                    .map(employee -> Response.ok(employee).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorResponse("Employee not found with ID: " + id))
                            .build());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchByName(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Search parameter 'q' cannot be empty"))
                    .build();
        }

        try {
            List<EmployeeResponse> results = employeeService.searchByName(query);
            return Response.ok(results).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/role/{role}")
    public Response findByRole(@PathParam("role") String roleStr) {
        try {
            EmployeeRole role = EmployeeRole.valueOf(roleStr.toUpperCase());
            List<EmployeeResponse> employees = employeeService.findByRole(role);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid role: " + roleStr))
                    .build();
        }
    }

    @GET
    @Path("/employment-type/{type}")
    public Response findByEmploymentType(@PathParam("type") String typeStr) {
        try {
            EmploymentType type = EmploymentType.valueOf(typeStr.toUpperCase());
            List<EmployeeResponse> employees = employeeService.findByEmploymentType(type);
            return Response.ok(employees).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid employment type: " + typeStr))
                    .build();
        }
    }

    @GET
    @Path("/{id}/subordinates")
    public Response getSubordinates(@PathParam("id") String idStr) {
        try {
            UUID managerId = UUID.fromString(idStr);
            List<EmployeeResponse> subordinates = employeeService.getSubordinates(managerId);
            return Response.ok(subordinates).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Transactional
    public Response create(@Valid EmployeeResponse dto) {
        try {
            EmployeeResponse created = employeeService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String idStr, @Valid EmployeeResponse dto) {
        try {
            UUID id = UUID.fromString(idStr);
            EmployeeResponse updated = employeeService.update(id, dto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);
            employeeService.delete(id);
            return Response.ok()
                    .entity(new SuccessResponse("Employee deleted successfully"))
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

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
                    .entity(new SuccessResponse("Manager assigned successfully"))
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{employeeId}/manager")
    @Transactional
    public Response removeManager(@PathParam("employeeId") String employeeIdStr) {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            employeeService.removeManager(employeeId);
            return Response.ok()
                    .entity(new SuccessResponse("Manager removed successfully"))
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/stats")
    public Response getStatistics() {
        long totalActive = employeeService.countActiveEmployees();
        return Response.ok()
                .entity(new StatsResponse(totalActive))
                .build();
    }

    @GET
    @Path("/stats/role/{role}")
    public Response getStatsByRole(@PathParam("role") String roleStr) {
        try {
            EmployeeRole role = EmployeeRole.valueOf(roleStr.toUpperCase());
            long count = employeeService.countByRole(role);
            return Response.ok()
                    .entity(new CountResponse(count))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid role: " + roleStr))
                    .build();
        }
    }

    @GET
    @Path("/stats/employment-type/{type}")
    public Response getStatsByEmploymentType(@PathParam("type") String typeStr) {
        try {
            EmploymentType type = EmploymentType.valueOf(typeStr.toUpperCase());
            long count = employeeService.countByEmploymentType(type);
            return Response.ok()
                    .entity(new CountResponse(count))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid employment type: " + typeStr))
                    .build();
        }
    }

    public record ErrorResponse(String error) {}

    public record SuccessResponse(String message) {}

    public record StatsResponse(long totalActiveEmployees) {}

    public record CountResponse(long count) {}
}