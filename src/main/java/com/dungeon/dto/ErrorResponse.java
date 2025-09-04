package com.dungeon.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for error responses using Java 23 record.
 */
@Schema(description = "Error response for failed dungeon solving requests")
public record ErrorResponse(
    @Schema(description = "HTTP status code", example = "400")
    int status,
    
    @Schema(description = "Error message", example = "Invalid dungeon: must not be empty")
    String message,
    
    @Schema(description = "Error code for categorization", example = "INVALID_INPUT")
    String errorCode,
    
    @Schema(description = "Request timestamp", example = "2024-01-15T10:30:00Z")
    String timestamp,
    
    @Schema(description = "Request path", example = "/api/dungeon/solve")
    String path
) {
    
    /**
     * Creates an error response with validation.
     * 
     * @param status HTTP status code
     * @param message error message
     * @param errorCode error classification code
     * @param timestamp request timestamp
     * @param path request path
     */
    public ErrorResponse {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        if (errorCode == null || errorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Error code cannot be null or empty");
        }
    }
}
