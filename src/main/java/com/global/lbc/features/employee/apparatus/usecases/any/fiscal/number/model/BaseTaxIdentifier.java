package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.model;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxIdentifier;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxValidationStrategy;

// ============================================================
// STRATEGY + FACTORY: BaseTaxIdentifier (Domain Value Object)
// Abstract class: cannot be instantiated, serves as a template for subclasses.
// It can have both implemented and abstract methods (without a body),
// bringing together common behavior and imposing contracts on child classes.
// Ensures invariance during creation: validates the raw tax identification number using the TaxValidationStrategy.
// Delegates formatting, masking, and validation to the strategy, allowing multiple country formats (CPF, CNPJ, NIF, ...).
// Designed to be persisted as simple fields (raw value + country) and reconstructed by a factory or @PostLoad.
// It is used by the Use Case named (Any Tax Number) as a safe and immutable input/value object — it models
// data with behavior and has no side effects (it is not a service).
// To maintain the architecture of this Use Case named (Any Tax Number), prefer factories/builders to instantiate
// concrete identifiers so that this use case remains focused and clean.
// ============================================================

public abstract class BaseTaxIdentifier implements TaxIdentifier {

    protected final String value;
    protected final TaxValidationStrategy strategy;

    protected BaseTaxIdentifier(String value, TaxValidationStrategy strategy) {
        this.value = value;
        this.strategy = strategy;

        if (!strategy.validate(value)) {
            throw new IllegalArgumentException("Invalid tax identifier: " + value);
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getFormatted() {
        return strategy.format(value);
    }

    @Override
    public String getMasked() {
        return strategy.mask(value);
    }

    @Override
    public boolean isValid() {
        return strategy.validate(value);
    }
}