package com.dungeon.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Sealed class representing the result of a dungeon solving operation.
 * Uses Java 23 sealed classes to provide exhaustive pattern matching.
 */
@Schema(description = "Result of dungeon solving operation")
public sealed interface DungeonSolveResult 
    permits DungeonSolveResult.Success, DungeonSolveResult.Failure {
    
    /**
     * Successful result containing the solution.
     * 
     * @param input the original dungeon grid
     * @param path the optimal path from start to end
     * @param minHp the minimum initial health required
     */
    @Schema(description = "Successful solution result")
    record Success(
        @Schema(description = "Original dungeon grid input")
        int[][] input,
        
        @Schema(description = "Optimal path as list of positions")
        List<Position> path,
        
        @Schema(description = "Minimum initial health points required", example = "7", minimum = "1")
        int minHp
    ) implements DungeonSolveResult {
        
        public Success {
            if (input == null || input.length == 0) {
                throw new IllegalArgumentException("Input cannot be null or empty");
            }
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path cannot be null or empty");
            }
            if (minHp < 1) {
                throw new IllegalArgumentException("Minimum HP must be at least 1");
            }
        }
    }
    
    /**
     * Failed result with error information.
     * 
     * @param input the original dungeon grid (if available)
     * @param reason the reason for failure
     * @param errorCode error classification code
     */
    @Schema(description = "Failed solution result with error details")
    record Failure(
        @Schema(description = "Original dungeon grid input (if available)")
        int[][] input,
        
        @Schema(description = "Human-readable reason for failure")
        String reason,
        
        @Schema(description = "Error classification code")
        ErrorCode errorCode
    ) implements DungeonSolveResult {
        
        public Failure {
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Failure reason cannot be null or empty");
            }
            if (errorCode == null) {
                throw new IllegalArgumentException("Error code cannot be null");
            }
        }
    }
    
    /**
     * Error codes for categorizing failures.
     */
    @Schema(description = "Error codes for dungeon solving failures")
    enum ErrorCode {
        INVALID_INPUT("Input validation failed"),
        UNSOLVABLE("No valid path exists"),
        INFINITE_HP("Minimum HP calculation resulted in infinite value"),
        PROCESSING_ERROR("Internal processing error");
        
        private final String description;
        
        ErrorCode(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
