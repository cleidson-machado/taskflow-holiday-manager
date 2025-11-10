package com.global.lbc.features.employee.apparatus.usecases.pt.social.number;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jboss.logging.Logger;

@Converter(autoApply = true)
public class SocialNumberConverter implements AttributeConverter<SocialNumber, String> {

    //Track Issue: LBC-1234 to manage register data problems
    private static final Logger LOG = Logger.getLogger(SocialNumberConverter.class);

    @Override
    public String convertToDatabaseColumn(SocialNumber attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public SocialNumber convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return SocialNumber.of(dbData);
        } catch (IllegalArgumentException e) {

            LOG.warnf("❌ Failed to convert invalid social number '%s' from DB. Error: %s. Returning NULL to prevent API crash.", dbData, e.getMessage());
            return null;
        }
    }
}