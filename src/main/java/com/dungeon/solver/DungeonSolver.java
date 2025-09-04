package com.dungeon.solver;

import com.dungeon.model.Position;
import com.dungeon.model.DungeonSolveResult;
import java.util.List;

/**
 * Interface defining the contract for dungeon solving algorithms.
 * Part of the hexagonal architecture - this represents a port.
 */
public interface DungeonSolver {
    
    /**
     * Solves the dungeon traversal problem to find the minimum initial health
     * required for a knight to reach from top-left to bottom-right.
     * 
     * @param dungeon 2D array representing the dungeon grid
     * @return DungeonSolveResult containing either success with path and min HP, or failure with reason
     */
    DungeonSolveResult solve(int[][] dungeon);
    
    /**
     * Finds the optimal path through the dungeon with minimum health requirement.
     * 
     * @param dungeon 2D array representing the dungeon grid
     * @return List of positions representing the optimal path, or empty list if no solution
     */
    List<Position> findOptimalPath(int[][] dungeon);
    
    /**
     * Calculates the minimum initial health required for a given path.
     * 
     * @param dungeon 2D array representing the dungeon grid
     * @param path List of positions representing the path
     * @return minimum initial health required, or -1 if path is invalid
     */
    int calculateMinimumHealth(int[][] dungeon, List<Position> path);
}
