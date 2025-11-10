package com.global.lbc.features.employee.apparatus.application.dto;

import com.global.lbc.features.employee.apparatus.model.util.EmployeeRole;
import com.global.lbc.features.employee.apparatus.model.util.EmploymentType;
import com.global.lbc.features.employee.apparatus.usecases.pt.social.number.SocialNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmployeeResponse {

    public UUID id;
    public String name;
    public String surname;
    public String fiscalNumber;
    public String fiscalNumberCountry;
    public SocialNumber socialNumber;
    public LocalDate dateOfBirth;
    public EmploymentType employmentType;
    public EmployeeRole employeeRole;
    public LocalDate hireDate;
    public LocalDate terminationDate;
    public Float salaryBase;
    public Boolean isActive;
    public UUID managerId;
    public Long vacationDaysBalance;
    public Long vacationDaysUsed;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public EmployeeResponse() {
    }
}