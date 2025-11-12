package com.global.lbc.features.booking.apparatus.application.dto;

import com.global.lbc.features.booking.apparatus.model.util.BookingStatus;
import com.global.lbc.features.vacation.apparatus.application.dto.EmployeeResponseSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BookingResponse {

    public UUID id;
    public EmployeeResponseSummary employee;
    public UUID vacationId;
    public LocalDate startDate;
    public LocalDate endDate;
    public Integer daysReserved;
    public BookingStatus bookingStatus;
    public Boolean isActive;
    public String requestNotes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime deletedAt;
    public String deletedBy;

    public BookingResponse() {
    }
}

