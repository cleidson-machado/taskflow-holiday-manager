package com.global.lbc.features.booking.apparatus.application.dto;

import java.time.LocalDate;
import java.util.UUID;

// DTO para CREATE/UPDATE
public class BookingRequest {
    public UUID employeeId;
    public LocalDate startDate;
    public Integer daysReserved; // Número de dias úteis de férias solicitados
    public String requestNotes;
    // status, vacationId etc. não devem vir no request inicial

    public BookingRequest() {}
}
