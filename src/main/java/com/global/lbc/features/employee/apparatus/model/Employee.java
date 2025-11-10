package com.global.lbc.features.employee.apparatus.model;

import com.global.lbc.features.employee.apparatus.model.util.EmployeeRole;
import com.global.lbc.features.employee.apparatus.model.util.EmploymentType;
import com.global.lbc.features.employee.apparatus.usecases.pt.social.number.SocialNumber;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.factory.TaxIdentifierFactory;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxIdentifier;
import com.global.lbc.features.employee.apparatus.usecases.pt.social.number.SocialNumberConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// ============================================================
// ENTITY + STRATEGY + FACTORY: Employee (Domain Entity with JPA Persistence)
// Concrete class: can be instantiated and persisted; represents a complete entity with implemented state and behavior.
// Contains only concrete methods (e.g., @PrePersist/@PreUpdate/@PostLoad hooks) — it does not declare abstract methods,
// therefore, it does not impose mandatory contracts on subclasses (it can be extended, but it is not necessary for its use).
// Entity/Domain: Employee — JPA entity model (Quarkus + Panache) representing an employee's profile.
// Manages domain and persistence data (required fields, uniqueness, logical deletion, simple auditing).
// Uses the Strategy/Factory pattern for reconstruction and synchronization of tax identifiers
// and a value converter for the social security number.
// Defines relationships with controlled cascade behavior and lifecycle
// hooks for synchronization and timestamping.
// Responsible only for state and invariants; complex rules and
// validations are handled in factories or application services.
// ============================================================

@Entity
@Table(name = "employee_profile")
public class Employee extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "surname", nullable = false, length = 100)
    public String surname;

    // ============================================================
    // STRATEGY + FACTORY PATTERN: TaxIdentifier abstraction
    // Persists the raw value in the database (fiscal_number) and the country code (fiscal_number_country)
    // The TaxIdentifier is automatically reconstructed after loading from the database (@PostLoad)
    // Supports multiple formats (NIF, CPF, etc.) in a decoupled way
    // ============================================================
    @Column(name = "fiscal_number", unique = true, length = 20)
    public String fiscalNumber;

    @Column(name = "fiscal_number_country", length = 2)
    public String fiscalNumberCountry;

    @Transient
    private TaxIdentifier taxIdentifier;

    public TaxIdentifier getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(TaxIdentifier taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
        if (taxIdentifier != null) {
            this.fiscalNumber = taxIdentifier.getValue();
            this.fiscalNumberCountry = taxIdentifier.getCountryCode();
        } else {
            this.fiscalNumber = null;
            this.fiscalNumberCountry = null;
        }
    }
    // ============================================================

    @Column(name = "social_number", unique = true, length = 20)
    @Convert(converter = SocialNumberConverter.class)
    public SocialNumber socialNumber;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @Column(name = "contract_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public EmploymentType employmentType;

    @Column(name = "employee_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public EmployeeRole employeeRole;

    @Column(name = "hire_date", nullable = false)
    public LocalDate hireDate;

    @Column(name = "termination_date")
    public LocalDate terminationDate;

    @Column(name = "salary_base")
    public Float salaryBase;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    // soft delete fields ------------------------ start
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    public String deletedBy;
    // soft delete fields ------------------------ end

    // Automatic manager-subordinate relationship ----------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    public Employee manager;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Employee> subordinates = new HashSet<>();

    @Column(name = "vacation_days_balance")
    public Long vacationDaysBalance = 0L;

    @Column(name = "vacation_days_used")
    public Long vacationDaysUsed = 0L;

    // Deprecated fields - will be replaced by audit entity
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        syncTaxIdentifierToDatabase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        syncTaxIdentifierToDatabase();
    }

    @PostLoad
    protected void onLoad() {
        reconstructTaxIdentifier();
    }

    private void syncTaxIdentifierToDatabase() {
        if (taxIdentifier != null) {
            this.fiscalNumber = taxIdentifier.getValue();
            this.fiscalNumberCountry = taxIdentifier.getCountryCode();
        }
    }

    private void reconstructTaxIdentifier() {
        if (fiscalNumber != null && fiscalNumberCountry != null) {
            try {
                this.taxIdentifier = TaxIdentifierFactory.create(
                        fiscalNumberCountry,
                        fiscalNumber
                );
            } catch (Exception e) {
                this.taxIdentifier = null;
            }
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}