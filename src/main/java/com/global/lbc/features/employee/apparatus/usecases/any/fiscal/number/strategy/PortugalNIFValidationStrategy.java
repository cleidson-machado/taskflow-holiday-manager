package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.strategy;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxValidationStrategy;

public class PortugalNIFValidationStrategy implements TaxValidationStrategy {

    @Override
    public boolean validate(String value) {
        if (value == null || value.length() != 9) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 8; i++) {
                sum += Character.getNumericValue(value.charAt(i)) * (9 - i);
            }
            int checkDigit = 11 - (sum % 11);
            if (checkDigit >= 10) checkDigit = 0;

            return checkDigit == Character.getNumericValue(value.charAt(8));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String format(String value) {
        if (value == null || value.length() != 9) return value;
        return value.substring(0, 3) + " " + value.substring(3, 6) + " " + value.substring(6);
    }

    @Override
    public String mask(String value) {
        if (value == null || value.length() != 9) return value;
        return "***-***-" + value.substring(7);
    }
}