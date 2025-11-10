package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.model.BaseTaxIdentifier;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.strategy.BrazilCPFValidationStrategy;

public class BrazilCPF extends BaseTaxIdentifier {

    public BrazilCPF(String value) {
        super(value, new BrazilCPFValidationStrategy());
    }

    @Override
    public String getCountryCode() {
        return "BR";
    }
}