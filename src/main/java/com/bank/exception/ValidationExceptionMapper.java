package com.bank.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {

            LOG.debugf("Mapping validation exception to 400 Bad Request: %s", exception.getMessage());
            LOG.infof("ValidationExceptionMapper is handling a %s: %s", exception.getClass().getSimpleName(), exception.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(exception.getMessage()))
                    .build();
        }

        return null;
    }

    public record ErrorMessage(String message) {}
}