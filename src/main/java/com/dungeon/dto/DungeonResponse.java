package com.dungeon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Data Transfer Object for successful dungeon solution response using Java 23 record.
 */
@Schema(description = "Successful response containing the dungeon solution")
public record DungeonResponse(
    @Schema(
        description = "Original input dungeon grid",
        example = "[[-2, -3, 3], [-5, -10, 1], [10, 30, -5]]"
    )
    int[][] input,
    
    @Schema(
        description = "Optimal path from top-left to bottom-right as array of [row, col] coordinates",
        example = "[[0,0], [0,1], [0,2], [1,2], [2,2]]"
    )
    int[][] path,
    
    @Schema(
        description = "Minimum initial health points required",
        example = "7",
        minimum = "1"
    )
    int min_hp
) {
    
    /**
     * Creates a response with validation.
     * 
     * @param input original dungeon grid
     * @param path optimal path as list of positions
     * @param min_hp minimum health required
     */
    public DungeonResponse {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (min_hp < 1) {
            throw new IllegalArgumentException("Minimum HP must be at least 1");
        }
    }
    
    /**
     * Creates a DungeonResponse from a list of positions.
     * 
     * @param input the original dungeon grid
     * @param pathPositions list of Position objects
     * @param minHp minimum health required
     * @return new DungeonResponse
     */
    public static DungeonResponse fromPositions(int[][] input, List<com.dungeon.model.Position> pathPositions, int minHp) {
        int[][] pathArray = pathPositions.stream()
            .map(pos -> new int[]{pos.row(), pos.col()})
            .toArray(int[][]::new);
        
        return new DungeonResponse(input, pathArray, minHp);
    }
}
