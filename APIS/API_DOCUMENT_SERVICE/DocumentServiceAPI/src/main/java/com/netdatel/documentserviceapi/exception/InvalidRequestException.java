package com.netdatel.documentserviceapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends DocumentServiceException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}