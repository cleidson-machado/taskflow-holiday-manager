package com.global.lbc.features.vacation.apparatus.application.dto;

import com.global.lbc.features.employee.apparatus.model.Employee;
import com.global.lbc.features.vacation.apparatus.model.util.VacationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class VacationResponse {

    public UUID id;
    public EmployeeResponseSummary employee;
    public LocalDate startDate;
    public LocalDate endDate;
    public Integer daysRequested;
    public VacationStatus vacationStatus;
    public Boolean isActive;
    public String approvingBy;
    public LocalDateTime approvalDate;
    public String requestNotes;
    public String rejectionReason;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime deletedAt;
    public String deletedBy;

    public VacationResponse() {
    }
}
