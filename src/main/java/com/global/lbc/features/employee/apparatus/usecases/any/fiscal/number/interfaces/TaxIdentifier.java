package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces;

public interface TaxIdentifier {
    String getValue();
    String getFormatted();
    String getMasked();
    boolean isValid();
    String getCountryCode();
}
