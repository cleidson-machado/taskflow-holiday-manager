package com.global.lbc.features.human.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "Sobrenome é obrigatório")
    @Size(min = 2, max = 100, message = "Sobrenome deve ter entre 2 e 100 caracteres")
    private String surname;

    @NotBlank(message = "NIF é obrigatório")
    private String fiscalNumber;

    @NotBlank(message = "NISS é obrigatório")
    private String socialNumber;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dateOfBirth;

    @NotNull(message = "Tipo de contrato é obrigatório")
    private TypeOfContractEnum contractRole;

    @NotNull(message = "Cargo é obrigatório")
    private EmployeeRoleEnum employeeRole;

    @NotNull(message = "Data de contratação é obrigatória")
    private LocalDate hireDate;

    private LocalDate terminationDate;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean isActive;

    @NotNull(message = "Salário base é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salário deve ser maior que zero")
    private BigDecimal salaryBase;

    private String managerId;

    @Min(value = 0, message = "Saldo de férias não pode ser negativo")
    private Integer vacationDaysBalance;

    @Min(value = 0, message = "Dias de férias usados não pode ser negativo")
    private Integer vacationDaysUsed;

    // Construtor
    public EmployeeRequestDTO() {
        this.isActive = true;
        this.vacationDaysBalance = 22;
        this.vacationDaysUsed = 0;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFiscalNumber() {
        return fiscalNumber;
    }

    public void setFiscalNumber(String fiscalNumber) {
        this.fiscalNumber = fiscalNumber;
    }

    public String getSocialNumber() {
        return socialNumber;
    }

    public void setSocialNumber(String socialNumber) {
        this.socialNumber = socialNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public TypeOfContractEnum getContractRole() {
        return contractRole;
    }

    public void setContractRole(TypeOfContractEnum contractRole) {
        this.contractRole = contractRole;
    }

    // ✅ CORRIGIDO
    public EmployeeRoleEnum getEmployeeRole() {
        return employeeRole;
    }

    // ✅ CORRIGIDO
    public void setEmployeeRole(EmployeeRoleEnum employeeRole) {
        this.employeeRole = employeeRole;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getSalaryBase() {
        return salaryBase;
    }

    public void setSalaryBase(BigDecimal salaryBase) {
        this.salaryBase = salaryBase;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public Integer getVacationDaysBalance() {
        return vacationDaysBalance;
    }

    public void setVacationDaysBalance(Integer vacationDaysBalance) {
        this.vacationDaysBalance = vacationDaysBalance;
    }

    public Integer getVacationDaysUsed() {
        return vacationDaysUsed;
    }

    public void setVacationDaysUsed(Integer vacationDaysUsed) {
        this.vacationDaysUsed = vacationDaysUsed;
    }
}