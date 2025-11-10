package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.model.BaseTaxIdentifier;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.strategy.PortugalNIFValidationStrategy;

public class PortugalNIF extends BaseTaxIdentifier {

    public PortugalNIF(String value) {
        super(value, new PortugalNIFValidationStrategy());
    }

    @Override
    public String getCountryCode() {
        return "PT";
    }
}