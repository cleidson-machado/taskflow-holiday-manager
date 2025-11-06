package com.global.lbc.util;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException exception) {
        Map<String, Object> errorResponse = new HashMap<>();

        // Verifica se é uma violação de constraint
        if (exception.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception.getCause();
            String constraintName = cve.getConstraintName();

            errorResponse.put("error", "Constraint Violation");
            errorResponse.put("message", buildUserFriendlyMessage(constraintName));
            errorResponse.put("constraint", constraintName);

            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        }

        // Tratamento genérico para outras exceções de persistência
        errorResponse.put("error", "Database Error");
        errorResponse.put("message", "Ocorreu um erro ao processar a operação no banco de dados");
        errorResponse.put("details", exception.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }

    private String buildUserFriendlyMessage(String constraintName) {
        if (constraintName == null) {
            return "Violação de restrição do banco de dados";
        }

        if (constraintName.contains("fiscal_number")) {
            return "Este NIF já está cadastrado no sistema";
        }

        if (constraintName.contains("social_number")) {
            return "Este NISS já está cadastrado no sistema";
        }

        if (constraintName.contains("email")) {
            return "Este email já está cadastrado no sistema";
        }

        return "Violação de restrição: " + constraintName;
    }
}