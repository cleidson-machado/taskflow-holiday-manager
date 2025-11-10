package com.global.lbc.features.employee.apparatus.usecases.pt.social.number;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class SocialNumberDeserializer extends JsonDeserializer<SocialNumber> {

    @Override
    public SocialNumber deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Se vier como objeto com "value"
        if (node.has("value")) {
            return SocialNumber.of(node.get("value").asText());
        }

        // Se vier como string simples
        if (node.isTextual()) {
            return SocialNumber.of(node.asText());
        }

        throw new IllegalArgumentException("Invalid SocialNumber format");
    }
}