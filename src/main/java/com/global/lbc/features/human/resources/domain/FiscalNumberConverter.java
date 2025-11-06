package com.global.lbc.features.human.resources.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter para FiscalNumber (NIF).
 * Converte entre o Value Object FiscalNumber e String no banco de dados.
 */
@Converter(autoApply = true)
public class FiscalNumberConverter implements AttributeConverter<FiscalNumber, String> {

    @Override
    public String convertToDatabaseColumn(FiscalNumber fiscalNumber) {
        if (fiscalNumber == null) {
            return null;
        }
        return fiscalNumber.getValue();
    }

    @Override
    public FiscalNumber convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return FiscalNumber.of(dbData);
    }
}