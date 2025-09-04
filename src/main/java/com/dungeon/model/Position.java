package com.dungeon.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a position in the dungeon grid using Java 23 record.
 * Immutable data structure for coordinates.
 * 
 * @param row the row index (0-based)
 * @param col the column index (0-based)
 */
@Schema(description = "Position coordinates in the dungeon grid")
public record Position(
    @Schema(description = "Row index (0-based)", example = "0", minimum = "0")
    int row,
    
    @Schema(description = "Column index (0-based)", example = "0", minimum = "0")
    int col
) {
    
    /**
     * Creates a new position with validation.
     * 
     * @param row must be non-negative
     * @param col must be non-negative
     */
    public Position {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Position coordinates must be non-negative");
        }
    }
    
    /**
     * Checks if this position is adjacent to another position (right or down only).
     * 
     * @param other the other position to check
     * @return true if positions are adjacent in valid directions
     */
    public boolean isAdjacentTo(Position other) {
        return (this.row == other.row && this.col + 1 == other.col) ||
               (this.col == other.col && this.row + 1 == other.row);
    }
    
    /**
     * Creates a position to the right of this one.
     * 
     * @return new position one column to the right
     */
    public Position moveRight() {
        return new Position(row, col + 1);
    }
    
    /**
     * Creates a position below this one.
     * 
     * @return new position one row down
     */
    public Position moveDown() {
        return new Position(row + 1, col);
    }
}
