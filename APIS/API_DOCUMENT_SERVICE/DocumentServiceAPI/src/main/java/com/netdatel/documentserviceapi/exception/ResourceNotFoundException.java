package com.netdatel.documentserviceapi.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends DocumentServiceException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}