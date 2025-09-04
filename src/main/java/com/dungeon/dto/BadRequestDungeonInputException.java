package com.dungeon.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for invalid dungeon input format.
 * This exception is thrown when the input cannot be parsed as a 2D integer array.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestDungeonInputException extends RuntimeException {

    private final String errorCode;

    public BadRequestDungeonInputException(String message) {
        super(message);
        this.errorCode = "INVALID_DUNGEON_FORMAT";
    }

    public BadRequestDungeonInputException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BadRequestDungeonInputException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INVALID_DUNGEON_FORMAT";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
