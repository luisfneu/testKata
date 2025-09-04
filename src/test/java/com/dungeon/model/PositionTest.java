package com.dungeon.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Position record.
 */
@DisplayName("Position Tests")
class PositionTest {
    
    @Test
    @DisplayName("Should create valid position with non-negative coordinates")
    void shouldCreateValidPosition() {
        Position position = new Position(2, 3);
        
        assertEquals(2, position.row());
        assertEquals(3, position.col());
    }
    
    @Test
    @DisplayName("Should create position at origin")
    void shouldCreatePositionAtOrigin() {
        Position position = new Position(0, 0);
        
        assertEquals(0, position.row());
        assertEquals(0, position.col());
    }
    
    @Test
    @DisplayName("Should throw exception for negative row")
    void shouldThrowExceptionForNegativeRow() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
    }
    
    @Test
    @DisplayName("Should throw exception for negative column")
    void shouldThrowExceptionForNegativeColumn() {
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
    }
    
    @Test
    @DisplayName("Should throw exception for both negative coordinates")
    void shouldThrowExceptionForBothNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, -1));
    }
    
    @Test
    @DisplayName("Should detect adjacent position to the right")
    void shouldDetectAdjacentPositionToRight() {
        Position current = new Position(1, 1);
        Position right = new Position(1, 2);
        
        assertTrue(current.isAdjacentTo(right));
    }
    
    @Test
    @DisplayName("Should detect adjacent position below")
    void shouldDetectAdjacentPositionBelow() {
        Position current = new Position(1, 1);
        Position below = new Position(2, 1);
        
        assertTrue(current.isAdjacentTo(below));
    }
    
    @Test
    @DisplayName("Should not detect non-adjacent position")
    void shouldNotDetectNonAdjacentPosition() {
        Position current = new Position(1, 1);
        Position diagonal = new Position(2, 2);
        Position distant = new Position(3, 1);
        Position left = new Position(1, 0);
        Position above = new Position(0, 1);
        
        assertFalse(current.isAdjacentTo(diagonal));
        assertFalse(current.isAdjacentTo(distant));
        assertFalse(current.isAdjacentTo(left));
        assertFalse(current.isAdjacentTo(above));
    }
    
    @Test
    @DisplayName("Should move right correctly")
    void shouldMoveRightCorrectly() {
        Position current = new Position(2, 3);
        Position expected = new Position(2, 4);
        
        assertEquals(expected, current.moveRight());
    }
    
    @Test
    @DisplayName("Should move down correctly")
    void shouldMoveDownCorrectly() {
        Position current = new Position(2, 3);
        Position expected = new Position(3, 3);
        
        assertEquals(expected, current.moveDown());
    }
    
    @Test
    @DisplayName("Should maintain equality semantics")
    void shouldMaintainEqualitySemantics() {
        Position pos1 = new Position(1, 2);
        Position pos2 = new Position(1, 2);
        Position pos3 = new Position(2, 1);
        
        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());
        assertNotEquals(pos1, pos3);
    }
}
