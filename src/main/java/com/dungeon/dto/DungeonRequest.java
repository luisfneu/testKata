package com.dungeon.dto;

import com.dungeon.validation.ValidDungeon;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for dungeon input using Java 23 record.
 * Contains validation annotations for input validation.
 */
@Schema(description = "Request object containing the dungeon grid to solve")
@JsonDeserialize(using = DungeonRequestDeserializer.class)
public record DungeonRequest(
    @NotNull(message = "Input dungeon cannot be null")
    @ValidDungeon
    @Schema(
        description = "2D array representing the dungeon grid",
        example = "[[-2, -3, 3], [-5, -10, 1], [10, 30, -5]]",
        required = true
    )
    int[][] input
) {
    
    /**
     * Creates a dungeon request with basic validation.
     * 
     * @param input the dungeon grid, must not be null
     */
    public DungeonRequest {
        // Basic null check - detailed validation is done by @ValidDungeon
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
    }
    
    /**
     * Gets the number of rows in the dungeon.
     * 
     * @return number of rows
     */
    public int getRows() {
        return input.length;
    }
    
    /**
     * Gets the number of columns in the dungeon.
     * 
     * @return number of columns
     */
    public int getCols() {
        return input.length > 0 ? input[0].length : 0;
    }
    
    /**
     * Checks if the dungeon is empty.
     * 
     * @return true if the dungeon has no cells
     */
    public boolean isEmpty() {
        return input.length == 0 || (input.length > 0 && input[0].length == 0);
    }
}
