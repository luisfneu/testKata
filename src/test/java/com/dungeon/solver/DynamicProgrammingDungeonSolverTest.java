package com.dungeon.solver;

import com.dungeon.model.DungeonSolveResult;
import com.dungeon.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the DynamicProgrammingDungeonSolver.
 */
@DisplayName("Dynamic Programming Dungeon Solver Tests")
class DynamicProgrammingDungeonSolverTest {
    
    private DynamicProgrammingDungeonSolver solver;
    
    @BeforeEach
    void setUp() {
        solver = new DynamicProgrammingDungeonSolver();
    }
    
    @Test
    @DisplayName("Should solve simple 3x3 dungeon from example")
    void shouldSolveSimple3x3Dungeon() {
        int[][] dungeon = {
            {-2, -3, 3},
            {-5, -10, 1},
            {10, 30, -5}
        };
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Success.class, result);
        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result;
        
        assertEquals(7, success.minHp());
        assertNotNull(success.path());
        assertEquals(5, success.path().size()); // Start to end should be 5 steps
        
        // Verify path starts and ends correctly
        assertEquals(new Position(0, 0), success.path().get(0));
        assertEquals(new Position(2, 2), success.path().get(success.path().size() - 1));
    }
    
    @Test
    @DisplayName("Should solve 1x1 dungeon")
    void shouldSolve1x1Dungeon() {
        int[][] dungeon = {{-3}};
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Success.class, result);
        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result;
        
        assertEquals(4, success.minHp()); // Need 4 HP to survive -3 damage
        assertEquals(1, success.path().size());
        assertEquals(new Position(0, 0), success.path().get(0));
    }
    
    @Test
    @DisplayName("Should solve 1x1 dungeon with positive value")
    void shouldSolve1x1DungeonWithPositiveValue() {
        int[][] dungeon = {{5}};
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Success.class, result);
        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result;
        
        assertEquals(1, success.minHp()); // Minimum HP is always 1
        assertEquals(1, success.path().size());
    }
    
    @Test
    @DisplayName("Should solve dungeon with all positive values")
    void shouldSolveDungeonWithAllPositiveValues() {
        int[][] dungeon = {
            {1, 2, 3},
            {4, 5, 6}
        };
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Success.class, result);
        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result;
        
        assertEquals(1, success.minHp());
        assertNotNull(success.path());
    }
    
    @Test
    @DisplayName("Should solve dungeon with mixed values")
    void shouldSolveDungeonWithMixedValues() {
        int[][] dungeon = {
            {0, -3, 0},
            {-1, 0, -2},
            {2, 0, 0}
        };
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Success.class, result);
        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result;
        
        assertTrue(success.minHp() >= 1);
        assertNotNull(success.path());
    }
    
    @Test
    @DisplayName("Should handle null input")
    void shouldHandleNullInput() {
        DungeonSolveResult result = solver.solve(null);
        
        assertInstanceOf(DungeonSolveResult.Failure.class, result);
        DungeonSolveResult.Failure failure = (DungeonSolveResult.Failure) result;
        
        assertEquals(DungeonSolveResult.ErrorCode.INVALID_INPUT, failure.errorCode());
    }
    
    @Test
    @DisplayName("Should handle empty dungeon")
    void shouldHandleEmptyDungeon() {
        int[][] dungeon = {};
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Failure.class, result);
        DungeonSolveResult.Failure failure = (DungeonSolveResult.Failure) result;
        
        assertEquals(DungeonSolveResult.ErrorCode.INVALID_INPUT, failure.errorCode());
    }
    
    @Test
    @DisplayName("Should handle dungeon with empty rows")
    void shouldHandleDungeonWithEmptyRows() {
        int[][] dungeon = {{}};
        
        DungeonSolveResult result = solver.solve(dungeon);
        
        assertInstanceOf(DungeonSolveResult.Failure.class, result);
        DungeonSolveResult.Failure failure = (DungeonSolveResult.Failure) result;
        
        assertEquals(DungeonSolveResult.ErrorCode.INVALID_INPUT, failure.errorCode());
    }
    
    @Test
    @DisplayName("Should find optimal path for simple dungeon")
    void shouldFindOptimalPathForSimpleDungeon() {
        int[][] dungeon = {
            {-2, -3, 3},
            {-5, -10, 1},
            {10, 30, -5}
        };
        
        List<Position> path = solver.findOptimalPath(dungeon);
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(new Position(0, 0), path.get(0));
        assertEquals(new Position(2, 2), path.get(path.size() - 1));
        
        // Verify path validity (only right and down moves)
        for (int i = 1; i < path.size(); i++) {
            Position prev = path.get(i - 1);
            Position curr = path.get(i);
            assertTrue(prev.isAdjacentTo(curr), 
                "Invalid move from " + prev + " to " + curr);
        }
    }
    
    @Test
    @DisplayName("Should calculate minimum health for valid path")
    void shouldCalculateMinimumHealthForValidPath() {
        int[][] dungeon = {
            {-2, -3, 3},
            {-5, -10, 1},
            {10, 30, -5}
        };
        
        List<Position> path = List.of(
            new Position(0, 0),
            new Position(0, 1),
            new Position(0, 2),
            new Position(1, 2),
            new Position(2, 2)
        );
        
        int minHealth = solver.calculateMinimumHealth(dungeon, path);
        
        assertTrue(minHealth > 0);
    }
    
    @Test
    @DisplayName("Should return -1 for invalid path")
    void shouldReturnNegativeOneForInvalidPath() {
        int[][] dungeon = {{1, 2}, {3, 4}};
        
        List<Position> invalidPath = List.of(
            new Position(0, 0),
            new Position(1, 1) // Diagonal move - invalid
        );
        
        int result = solver.calculateMinimumHealth(dungeon, invalidPath);
        
        assertEquals(-1, result);
    }
    
    @Test
    @DisplayName("Should return empty list for unsolvable dungeon")
    void shouldReturnEmptyListForUnsolvableDungeon() {
        List<Position> path = solver.findOptimalPath(null);
        
        assertTrue(path.isEmpty());
    }
}
