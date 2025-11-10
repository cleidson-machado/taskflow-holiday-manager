package com.global.lbc.features.employee.apparatus.usecases.pt.social.number;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SocialNumberSerializer extends JsonSerializer<SocialNumber> {

    @Override
    public void serialize(SocialNumber value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("value", value.getValue());
        gen.writeStringField("formatted", value.getFormatted());
        gen.writeStringField("partiallyMasked", value.getPartiallyMasked());
        gen.writeStringField("masked", value.getMasked());
        gen.writeEndObject();
    }
}