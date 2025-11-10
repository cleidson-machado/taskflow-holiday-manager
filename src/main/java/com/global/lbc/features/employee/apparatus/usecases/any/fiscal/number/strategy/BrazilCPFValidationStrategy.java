package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.strategy;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxValidationStrategy;

public class BrazilCPFValidationStrategy implements TaxValidationStrategy {

    @Override
    public boolean validate(String value) {
        if (value == null || value.length() != 11) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(value.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(value.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;

            return firstDigit == Character.getNumericValue(value.charAt(9)) &&
                    secondDigit == Character.getNumericValue(value.charAt(10));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String format(String value) {
        if (value == null || value.length() != 11) return value;
        return value.substring(0, 3) + "." + value.substring(3, 6) + "." +
                value.substring(6, 9) + "-" + value.substring(9);
    }

    @Override
    public String mask(String value) {
        if (value == null || value.length() != 11) return value;
        return "***.***.***-" + value.substring(9);
    }
}