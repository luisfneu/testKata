package com.dungeon.service;

/**
 * Custom exception for dungeon solving operations.
 * Provides specific error information for different failure scenarios.
 */
public class DungeonSolvingException extends Exception {
    
    private final String errorCode;
    private final int[][] dungeonInput;
    
    /**
     * Creates a new DungeonSolvingException.
     * 
     * @param message the error message
     * @param errorCode the error code for categorization
     */
    public DungeonSolvingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.dungeonInput = null;
    }
    
    /**
     * Creates a new DungeonSolvingException with dungeon input context.
     * 
     * @param message the error message
     * @param errorCode the error code for categorization
     * @param dungeonInput the dungeon input that caused the error
     */
    public DungeonSolvingException(String message, String errorCode, int[][] dungeonInput) {
        super(message);
        this.errorCode = errorCode;
        this.dungeonInput = dungeonInput;
    }
    
    /**
     * Creates a new DungeonSolvingException with a cause.
     * 
     * @param message the error message
     * @param errorCode the error code for categorization
     * @param cause the underlying cause
     */
    public DungeonSolvingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.dungeonInput = null;
    }
    
    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the dungeon input that caused this exception.
     * 
     * @return the dungeon input, or null if not available
     */
    public int[][] getDungeonInput() {
        return dungeonInput;
    }
}
