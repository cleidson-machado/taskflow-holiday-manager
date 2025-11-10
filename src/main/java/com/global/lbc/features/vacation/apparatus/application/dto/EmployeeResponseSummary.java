package com.global.lbc.features.vacation.apparatus.application.dto;

import java.util.UUID;

public class EmployeeResponseSummary {

    public UUID id;
    public String name;
    public String surname;
    public String fiscalNumber;

    public EmployeeResponseSummary() {
    }

    public EmployeeResponseSummary(UUID id, String name, String surname, String fiscalNumber) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.fiscalNumber = fiscalNumber;
    }
}
