package com.netdatel.documentserviceapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DocumentServiceException extends RuntimeException {
    private final HttpStatus status;

    public DocumentServiceException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DocumentServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public DocumentServiceException(String message, Throwable cause) {
        this(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DocumentServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
