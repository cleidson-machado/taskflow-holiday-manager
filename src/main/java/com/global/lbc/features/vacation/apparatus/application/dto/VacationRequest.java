package com.global.lbc.features.vacation.apparatus.application.dto;

import com.global.lbc.features.vacation.apparatus.model.util.VacationStatus;
import java.time.LocalDate;
import java.util.UUID;

// DTO para CREATE/UPDATE
public class VacationRequest {
    public UUID employeeId;
    public LocalDate startDate;
    public LocalDate endDate;
    public String requestNotes;
    // status, daysRequested, approvingBy etc. não devem vir no request inicial

    public VacationRequest() {}
}
