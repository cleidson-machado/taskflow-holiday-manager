package com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.factory;

import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.BrazilCPF;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.PortugalNIF;
import com.global.lbc.features.employee.apparatus.usecases.any.fiscal.number.interfaces.TaxIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TaxIdentifierFactory {

    private static final Map<String, Function<String, TaxIdentifier>> creators = new HashMap<>();

    static {
        creators.put("PT", PortugalNIF::new);
        creators.put("BR", BrazilCPF::new);
    }

    public static TaxIdentifier create(String countryCode, String value) {
        Function<String, TaxIdentifier> creator = creators.get(countryCode.toUpperCase());

        if (creator == null) {
            throw new IllegalArgumentException("Unsupported country code: " + countryCode);
        }

        return creator.apply(value);
    }

    public static void registerCountry(String countryCode, Function<String, TaxIdentifier> creator) {
        creators.put(countryCode.toUpperCase(), creator);
    }
}