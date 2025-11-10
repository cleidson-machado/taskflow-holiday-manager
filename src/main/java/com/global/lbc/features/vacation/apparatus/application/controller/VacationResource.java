package com.global.lbc.features.vacation.apparatus.application.controller;

import com.global.lbc.features.vacation.apparatus.application.dto.VacationRequest;
import com.global.lbc.features.vacation.apparatus.application.dto.VacationResponse;
import com.global.lbc.features.vacation.apparatus.application.service.VacationService;
import com.global.lbc.shared.PaginatedResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/vacations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VacationResource {

    @Inject
    VacationService vacationService;

    // --- Endpoints de Consulta (READ) ---

    @GET
    public Response listVacationRequests(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("sortField") @DefaultValue("startDate") String sortField,
            @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder
    ) {
        try {
            PaginatedResponse<VacationResponse> response = vacationService.getPaginatedVacations(
                    page,
                    size,
                    sortField,
                    sortOrder
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getVacationById(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);

            return vacationService.findById(id)
                    .map(vacation -> Response.ok(vacation).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorResponse("Vacation request not found with ID: " + id))
                            .build());

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format: " + e.getMessage()))
                    .build();
        }
    }

    // --- Endpoints de Manipulação (CREATE & UPDATE) ---

    @POST
    @Transactional
    public Response create(@Valid VacationRequest dto) {
        try {
            VacationResponse created = vacationService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException | NotFoundException e) {
            // NotFoundException pode ser lançada pelo Mapper se o EmployeeId não existir
            Response.Status status = (e instanceof NotFoundException)
                    ? Response.Status.NOT_FOUND
                    : Response.Status.BAD_REQUEST;

            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String idStr, @Valid VacationRequest dto) {
        try {
            UUID id = UUID.fromString(idStr);
            VacationResponse updated = vacationService.update(id, dto);
            return Response.ok(updated).build();
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

    // --- Endpoints de Transição de Estado (APPROVE/REJECT) ---

    @PUT
    @Path("/{id}/approve")
    @Transactional
    public Response approve(
            @PathParam("id") String idStr,
            @QueryParam("approver") @DefaultValue("system") String approverName
    ) {
        try {
            UUID id = UUID.fromString(idStr);
            VacationResponse approved = vacationService.approve(id, approverName);
            return Response.ok(approved).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT) // 409 Conflict para status inválido
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format or missing approver: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/reject")
    @Transactional
    public Response reject(
            @PathParam("id") String idStr,
            @QueryParam("reason") @DefaultValue("No reason provided") String reason,
            @QueryParam("approver") @DefaultValue("system") String approverName
    ) {
        try {
            UUID id = UUID.fromString(idStr);
            VacationResponse rejected = vacationService.reject(id, approverName, reason);
            return Response.ok(rejected).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format or missing approver: " + e.getMessage()))
                    .build();
        }
    }

    // --- RECORDs de Resposta (Padrão EmployeeResource) ---

    public record ErrorResponse(String error) {}

    public record SuccessResponse(String message) {}
}