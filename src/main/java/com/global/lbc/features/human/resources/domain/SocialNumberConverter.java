package com.global.lbc.features.human.resources.domain;

import com.global.lbc.features.human.resources.domain.model.SocialNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter para SocialNumber (NISS).
 * Converte entre o Value Object SocialNumber e String no banco de dados.
 */
@Converter(autoApply = true)
public class SocialNumberConverter implements AttributeConverter<SocialNumber, String> {

    @Override
    public String convertToDatabaseColumn(SocialNumber socialNumber) {
        if (socialNumber == null) {
            return null;
        }
        return socialNumber.getValue();
    }

    @Override
    public SocialNumber convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return SocialNumber.of(dbData);
    }
}