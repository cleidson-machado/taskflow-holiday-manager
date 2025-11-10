package com.global.lbc.shared;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    // Padrão Regex para capturar o valor inválido ("XPTO") e a lista de valores aceitos ([...])
    // Padrão: from String "XPTO": not one of the values accepted for Enum class: [MANAGER, EMPLOYEE, ADMIN]
    private static final Pattern ENUM_DETAIL_PATTERN = Pattern.compile("from String \"(.*?)\": not one of the values accepted for Enum class: \\[([^\\]]+)\\]");

    @Override
    public Response toResponse(JsonMappingException exception) {
        Map<String, Object> errorResponse = new HashMap<>();

        if (exception instanceof InvalidFormatException ife) {

            String fieldName = getFieldName(exception);
            String userMessage = buildUserFriendlyMessage(exception.getOriginalMessage(), fieldName); // Usar getOriginalMessage()

            errorResponse.put("error", "Invalid Value Format");
            errorResponse.put("message", userMessage);
            errorResponse.put("field", fieldName);

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        // Tratamento genérico
        errorResponse.put("error", "Invalid Request");
        errorResponse.put("message", "The data submitted is in an invalid format");
        errorResponse.put("details", exception.getOriginalMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

    private String buildUserFriendlyMessage(String exceptionMessage, String fieldName) {
        if (exceptionMessage == null) {
            return "Invalid data format.";
        }

        // 1. Tenta capturar erro de ENUM (seu caso: "employeeRole": "XPTO")
        if (exceptionMessage.contains("not one of the values accepted for Enum class")) {

            Matcher matcher = ENUM_DETAIL_PATTERN.matcher(exceptionMessage);

            if (matcher.find() && matcher.groupCount() >= 2) {
                String invalidValue = matcher.group(1); // Captura "XPTO"
                String validValues = matcher.group(2).replace(", ", " | "); // Captura "MANAGER, EMPLOYEE, ADMIN"

                return String.format(
                        "O valor '%s' é inválido para o campo '%s'. Por favor, use um dos seguintes valores: %s",
                        invalidValue,
                        fieldName,
                        validValues
                );
            }

            // Fallback se o regex não funcionar perfeitamente
            return "O valor fornecido para o campo '" + fieldName + "' é inválido. Verifique os valores aceitos na documentação.";
        }

        // 2. Trata Validações de Campos Específicos (como no seu código original)
        if (exceptionMessage.contains("NISS")) {
            return "O NISS fornecido é inválido. Deve conter 11 dígitos válidos.";
        }

        if (exceptionMessage.contains("NIF") || exceptionMessage.contains("fiscalNumber")) {
            return "O NIF fornecido é inválido. Deve conter 9 dígitos válidos.";
        }

        if (exceptionMessage.contains("date")) {
            return "Formato de data inválido. Use o formato: YYYY-MM-DD";
        }

        return "Formato de dados inválido para o campo " + fieldName + ".";
    }

    private String getFieldName(JsonMappingException exception) {
        if (exception.getPath() != null && !exception.getPath().isEmpty()) {
            return exception.getPath().get(0).getFieldName();
        }
        return "N/A (campo desconhecido)";
    }
}