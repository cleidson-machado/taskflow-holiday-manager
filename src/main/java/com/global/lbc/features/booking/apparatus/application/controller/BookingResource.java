package com.global.lbc.features.booking.apparatus.application.controller;

import com.global.lbc.features.booking.apparatus.application.dto.BookingRequest;
import com.global.lbc.features.booking.apparatus.application.dto.BookingResponse;
import com.global.lbc.features.booking.apparatus.application.service.BookingService;
import com.global.lbc.shared.PaginatedResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    BookingService bookingService;

    // --- Endpoints de Consulta (READ) ---

    @GET
    public Response listBookings(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("sortField") @DefaultValue("startDate") String sortField,
            @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder
    ) {
        try {
            PaginatedResponse<BookingResponse> response = bookingService.getPaginatedBookings(
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
    public Response getBookingById(@PathParam("id") String idStr) {
        try {
            UUID id = UUID.fromString(idStr);

            return bookingService.findById(id)
                    .map(booking -> Response.ok(booking).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorResponse("Booking not found with ID: " + id))
                            .build());

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/employee/{employeeId}")
    public Response getActiveBookingsByEmployee(@PathParam("employeeId") String employeeIdStr) {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            var bookings = bookingService.findActiveBookingsByEmployee(employeeId);
            return Response.ok(bookings).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid employee ID format: " + e.getMessage()))
                    .build();
        }
    }

    // --- Endpoints de Manipulação (CREATE & UPDATE) ---

    @POST
    @Transactional
    public Response create(@Valid BookingRequest dto) {
        try {
            BookingResponse created = bookingService.create(dto);
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
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String idStr, @Valid BookingRequest dto) {
        try {
            UUID id = UUID.fromString(idStr);
            BookingResponse updated = bookingService.update(id, dto);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // --- Endpoints de Transição de Estado (CANCEL / LINK TO VACATION) ---

    @PUT
    @Path("/{id}/cancel")
    @Transactional
    public Response cancel(
            @PathParam("id") String idStr,
            @QueryParam("cancelledBy") @DefaultValue("system") String cancelledBy
    ) {
        try {
            UUID id = UUID.fromString(idStr);
            BookingResponse cancelled = bookingService.cancel(id, cancelledBy);
            return Response.ok(cancelled).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/link-vacation/{vacationId}")
    @Transactional
    public Response linkToVacation(
            @PathParam("id") String bookingIdStr,
            @PathParam("vacationId") String vacationIdStr
    ) {
        try {
            UUID bookingId = UUID.fromString(bookingIdStr);
            UUID vacationId = UUID.fromString(vacationIdStr);
            BookingResponse linked = bookingService.linkToVacation(bookingId, vacationId);
            return Response.ok(linked).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format: " + e.getMessage()))
                    .build();
        }
    }

    // --- Endpoint de Soft Delete ---

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response softDelete(
            @PathParam("id") String idStr,
            @QueryParam("deletedBy") @DefaultValue("system") String deletedBy
    ) {
        try {
            UUID id = UUID.fromString(idStr);
            bookingService.softDelete(id, deletedBy);
            return Response.ok(new SuccessResponse("Booking soft deleted successfully")).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid ID format or deletedBy parameter: " + e.getMessage()))
                    .build();
        }
    }

    // --- RECORDs de Resposta (Padrão EmployeeResource) ---

    public record ErrorResponse(String error) {}

    public record SuccessResponse(String message) {}
}

