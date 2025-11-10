package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces;

public interface TaxValidationStrategy {
    boolean validate(String value);
    String format(String value);
    String mask(String value);
}
