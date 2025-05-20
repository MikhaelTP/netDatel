package com.netdatel.documentserviceapi.exception;

import org.springframework.http.HttpStatus;

public class PermissionDeniedException extends DocumentServiceException {
    public PermissionDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
