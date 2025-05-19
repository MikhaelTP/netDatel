package com.netdatel.adminserviceapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se supera el límite de almacenamiento asignado.
 * Retorna un status HTTP 400 (Bad Request).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StorageLimitExceededException extends RuntimeException {

    private final Long clientId;
    private final Long allocatedBytes;
    private final Long requiredBytes;

    public StorageLimitExceededException(Long clientId, Long allocatedBytes, Long requiredBytes) {
        super(String.format("Límite de almacenamiento excedido para el cliente %d. Asignado: %d bytes, Requerido: %d bytes",
                clientId, allocatedBytes, requiredBytes));
        this.clientId = clientId;
        this.allocatedBytes = allocatedBytes;
        this.requiredBytes = requiredBytes;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getAllocatedBytes() {
        return allocatedBytes;
    }

    public Long getRequiredBytes() {
        return requiredBytes;
    }
}