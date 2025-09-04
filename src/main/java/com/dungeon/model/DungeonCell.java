package com.dungeon.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a cell in the dungeon using Java 23 record.
 * Each cell has a position and a value representing its effect on the knight's health.
 * 
 * @param position the position of this cell in the dungeon
 * @param value the health effect of this cell (negative = damage, positive = healing, zero = neutral)
 */
@Schema(description = "A cell in the dungeon grid with its position and health effect")
public record DungeonCell(
    @Schema(description = "Position of the cell in the grid")
    Position position,
    
    @Schema(description = "Health effect value", 
            example = "-5", 
            minimum = "-1000", 
            maximum = "100")
    int value
) {
    
    /**
     * Creates a dungeon cell with validation.
     * 
     * @param position must not be null
     * @param value must be within valid range [-1000, 100]
     */
    public DungeonCell {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (value < -1000 || value > 100) {
            throw new IllegalArgumentException("Cell value must be between -1000 and 100");
        }
    }
    
    /**
     * Checks if this cell causes damage to the knight.
     * 
     * @return true if the cell value is negative
     */
    public boolean isDamaging() {
        return value < 0;
    }
    
    /**
     * Checks if this cell heals the knight.
     * 
     * @return true if the cell value is positive
     */
    public boolean isHealing() {
        return value > 0;
    }
    
    /**
     * Checks if this cell is neutral (no effect).
     * 
     * @return true if the cell value is zero
     */
    public boolean isNeutral() {
        return value == 0;
    }
    
    /**
     * Gets the absolute value of the cell's effect.
     * 
     * @return absolute value of the cell's health effect
     */
    public int getAbsoluteValue() {
        return Math.abs(value);
    }
}
