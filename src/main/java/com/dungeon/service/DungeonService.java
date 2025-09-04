package com.dungeon.service;

import com.dungeon.dto.DungeonRequest;
import com.dungeon.dto.DungeonResponse;
import com.dungeon.model.DungeonSolveResult;

/**
 * Service interface for dungeon solving operations.
 * Part of the hexagonal architecture - this represents the application layer port.
 */
public interface DungeonService {
    
    /**
     * Solves a dungeon traversal problem and returns the solution.
     * 
     * @param request the dungeon request containing the grid to solve
     * @return DungeonResponse with the solution details
     * @throws DungeonSolvingException if the dungeon cannot be solved
     */
    DungeonResponse solveDungeon(DungeonRequest request) throws DungeonSolvingException;
    
    /**
     * Validates and processes a dungeon solving request.
     * 
     * @param dungeonGrid the raw dungeon grid
     * @return DungeonSolveResult containing success or failure information
     */
    DungeonSolveResult processDungeonSolving(int[][] dungeonGrid);
}
