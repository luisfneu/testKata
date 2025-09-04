package com.dungeon.solver;

import com.dungeon.model.Position;
import com.dungeon.model.DungeonSolveResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dynamic Programming implementation of the DungeonSolver interface.
 * Uses bottom-up approach to efficiently calculate minimum health requirements.
 * 
 * This implementation follows the SOLID principles:
 * - Single Responsibility: Only responsible for dungeon solving logic
 * - Open/Closed: Can be extended for different solving strategies
 * - Liskov Substitution: Can be substituted for any DungeonSolver implementation
 * - Interface Segregation: Implements only necessary methods
 * - Dependency Inversion: Depends on abstractions (Position, DungeonSolveResult)
 */
@Component
public class DynamicProgrammingDungeonSolver implements DungeonSolver {
    
    @Override
    public DungeonSolveResult solve(int[][] dungeon) {
        try {
            // Input validation
            if (dungeon == null || dungeon.length == 0 || dungeon[0].length == 0) {
                return new DungeonSolveResult.Failure(
                    dungeon, 
                    "Dungeon cannot be null or empty", 
                    DungeonSolveResult.ErrorCode.INVALID_INPUT
                );
            }
            
            int rows = dungeon.length;
            int cols = dungeon[0].length;
            
            // Calculate minimum health using dynamic programming
            int[][] minHealth = calculateMinHealthGrid(dungeon);
            
            // Check if solution exists
            if (minHealth[0][0] == Integer.MAX_VALUE) {
                return new DungeonSolveResult.Failure(
                    dungeon,
                    "No valid path exists through the dungeon",
                    DungeonSolveResult.ErrorCode.UNSOLVABLE
                );
            }
            
            // Reconstruct the optimal path
            List<Position> optimalPath = reconstructPath(dungeon, minHealth);
            
            int minInitialHealth = minHealth[0][0];
            
            return new DungeonSolveResult.Success(dungeon, optimalPath, minInitialHealth);
            
        } catch (Exception e) {
            return new DungeonSolveResult.Failure(
                dungeon,
                "Error processing dungeon: " + e.getMessage(),
                DungeonSolveResult.ErrorCode.PROCESSING_ERROR
            );
        }
    }
    
    @Override
    public List<Position> findOptimalPath(int[][] dungeon) {
        if (dungeon == null || dungeon.length == 0 || dungeon[0].length == 0) {
            return Collections.emptyList();
        }
        
        int[][] minHealth = calculateMinHealthGrid(dungeon);
        
        if (minHealth[0][0] == Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        
        return reconstructPath(dungeon, minHealth);
    }
    
    @Override
    public int calculateMinimumHealth(int[][] dungeon, List<Position> path) {
        if (dungeon == null || path == null || path.isEmpty()) {
            return -1;
        }
        
        if (!isValidPath(dungeon, path)) {
            return -1;
        }
        
        int minHealthNeeded = 1;
        int currentHealth = 1;
        
        for (Position pos : path) {
            int cellValue = dungeon[pos.row()][pos.col()];
            currentHealth += cellValue;
            
            // If health would drop to 0 or below, we need more initial health
            if (currentHealth <= 0) {
                int deficit = 1 - currentHealth;
                minHealthNeeded += deficit;
                currentHealth = 1;
            }
        }
        
        return minHealthNeeded;
    }
    
    /**
     * Calculates the minimum health grid using dynamic programming.
     * Works backwards from destination to source.
     * 
     * @param dungeon the dungeon grid
     * @return 2D array where minHealth[i][j] represents minimum health needed at position (i,j)
     */
    private int[][] calculateMinHealthGrid(int[][] dungeon) {
        int rows = dungeon.length;
        int cols = dungeon[0].length;
        
        // DP table: minHealth[i][j] = minimum health needed when entering cell (i,j)
        int[][] minHealth = new int[rows][cols];
        
        // Initialize with maximum values
        for (int i = 0; i < rows; i++) {
            Arrays.fill(minHealth[i], Integer.MAX_VALUE);
        }
        
        // Base case: destination cell
        // Health after entering destination = current health + dungeon[rows-1][cols-1]
        // We need this to be at least 1, so: current health >= 1 - dungeon[rows-1][cols-1]
        minHealth[rows - 1][cols - 1] = Math.max(1, 1 - dungeon[rows - 1][cols - 1]);
        
        // Fill the DP table from bottom-right to top-left
        for (int i = rows - 1; i >= 0; i--) {
            for (int j = cols - 1; j >= 0; j--) {
                // Skip if this is the destination (already calculated)
                if (i == rows - 1 && j == cols - 1) {
                    continue;
                }
                
                int minHealthFromRight = Integer.MAX_VALUE;
                int minHealthFromDown = Integer.MAX_VALUE;
                
                // Can move right?
                if (j + 1 < cols && minHealth[i][j + 1] != Integer.MAX_VALUE) {
                    minHealthFromRight = minHealth[i][j + 1];
                }
                
                // Can move down?
                if (i + 1 < rows && minHealth[i + 1][j] != Integer.MAX_VALUE) {
                    minHealthFromDown = minHealth[i + 1][j];
                }
                
                // If we can't reach destination from this cell, skip it
                if (minHealthFromRight == Integer.MAX_VALUE && minHealthFromDown == Integer.MAX_VALUE) {
                    continue;
                }
                
                // Take the minimum of the two possible moves
                int minHealthNeeded = Math.min(minHealthFromRight, minHealthFromDown);
                
                // Calculate minimum health needed when entering this cell
                // After picking up this cell's value, we need at least minHealthNeeded
                // So: currentHealth + dungeon[i][j] >= minHealthNeeded
                // Therefore: currentHealth >= minHealthNeeded - dungeon[i][j]
                // But currentHealth must be at least 1
                minHealth[i][j] = Math.max(1, minHealthNeeded - dungeon[i][j]);
            }
        }
        
        return minHealth;
    }
    
    /**
     * Reconstructs the optimal path by following the minimum health gradient.
     * 
     * @param dungeon the original dungeon grid
     * @param minHealth the calculated minimum health grid
     * @return list of positions representing the optimal path
     */
    private List<Position> reconstructPath(int[][] dungeon, int[][] minHealth) {
        List<Position> path = new ArrayList<>();
        int rows = dungeon.length;
        int cols = dungeon[0].length;
        
        int currentRow = 0;
        int currentCol = 0;
        
        path.add(new Position(currentRow, currentCol));
        
        // Follow the path by choosing the move that led to the current minimum health
        while (currentRow < rows - 1 || currentCol < cols - 1) {
            boolean canMoveRight = currentCol + 1 < cols;
            boolean canMoveDown = currentRow + 1 < rows;
            
            int rightHealth = canMoveRight ? minHealth[currentRow][currentCol + 1] : Integer.MAX_VALUE;
            int downHealth = canMoveDown ? minHealth[currentRow + 1][currentCol] : Integer.MAX_VALUE;
            
            // Choose the direction that gives minimum health requirement
            if (canMoveRight && (rightHealth <= downHealth || !canMoveDown)) {
                currentCol++;
            } else if (canMoveDown) {
                currentRow++;
            } else {
                // This shouldn't happen in a valid solution
                break;
            }
            
            path.add(new Position(currentRow, currentCol));
        }
        
        return path;
    }
    
    /**
     * Validates that a path is valid for the given dungeon.
     * 
     * @param dungeon the dungeon grid
     * @param path the path to validate
     * @return true if the path is valid
     */
    private boolean isValidPath(int[][] dungeon, List<Position> path) {
        if (path.isEmpty()) {
            return false;
        }
        
        int rows = dungeon.length;
        int cols = dungeon[0].length;
        
        // Check start and end positions
        Position start = path.get(0);
        Position end = path.get(path.size() - 1);
        
        if (!start.equals(new Position(0, 0)) || 
            !end.equals(new Position(rows - 1, cols - 1))) {
            return false;
        }
        
        // Check that each step is valid (only right or down moves)
        for (int i = 1; i < path.size(); i++) {
            Position prev = path.get(i - 1);
            Position curr = path.get(i);
            
            // Check bounds
            if (curr.row() < 0 || curr.row() >= rows || 
                curr.col() < 0 || curr.col() >= cols) {
                return false;
            }
            
            // Check valid move (only right or down)
            if (!prev.isAdjacentTo(curr)) {
                return false;
            }
        }
        
        return true;
    }
}
