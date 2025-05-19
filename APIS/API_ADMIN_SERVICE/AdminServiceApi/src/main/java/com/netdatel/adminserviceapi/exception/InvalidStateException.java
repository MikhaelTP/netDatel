package com.netdatel.adminserviceapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso ya no está en un estado válido para la operación solicitada.
 * Retorna un status HTTP 409 (Conflict).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateException extends RuntimeException {

    private final String currentState;
    private final String expectedState;

    public InvalidStateException(String message) {
        super(message);
        this.currentState = null;
        this.expectedState = null;
    }

    public InvalidStateException(String message, String currentState, String expectedState) {
        super(message);
        this.currentState = currentState;
        this.expectedState = expectedState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getExpectedState() {
        return expectedState;
    }
}