package com.zeynep.librarymanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Return HTTP 409 Conflict status
public class ISBNAlreadyExistsException extends RuntimeException {
    public ISBNAlreadyExistsException(String message) {
        super(message);
    }
}
