package com.global.lbc.features.booking.apparatus.application.dto;

import java.time.LocalDate;
import java.util.UUID;

// DTO para CREATE/UPDATE
public class BookingRequest {
    public UUID employeeId;
    public LocalDate startDate;
    public LocalDate endDate;
    public String requestNotes;
    // status, daysReserved, vacationId etc. não devem vir no request inicial

    public BookingRequest() {}
}

