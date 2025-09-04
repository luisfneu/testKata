package com.dungeon.service;

import com.dungeon.model.DungeonSolveResult;
import com.dungeon.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DungeonCacheService with new multi-table structure.
 */
@ExtendWith(MockitoExtension.class)
class DungeonCacheServiceTest {

    @Mock
    private DungeonDatabaseMapper databaseMapper;

    @InjectMocks
    private DungeonCacheService cacheService;

    private int[][] testDungeon;
    private DungeonSolveResult.Success testSuccessResult;
    private DungeonSolveResult.Failure testFailureResult;

    @BeforeEach
    void setUp() {
        testDungeon = new int[][]{
            {-3, 5},
            {1, -4}
        };
        
        testSuccessResult = new DungeonSolveResult.Success(
            testDungeon,
            List.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(1, 1)
            ),
            7
        );
        
        testFailureResult = new DungeonSolveResult.Failure(
            testDungeon,
            "No solution found",
            DungeonSolveResult.ErrorCode.UNSOLVABLE
        );
    }

    @Test
    void testComputeInputHash_SameDungeon_SameHash() {
        // Given
        int[][] dungeon1 = {{1, 2}, {3, 4}};
        int[][] dungeon2 = {{1, 2}, {3, 4}};

        // When
        String hash1 = cacheService.computeInputHash(dungeon1);
        String hash2 = cacheService.computeInputHash(dungeon2);

        // Then
        assertEquals(hash1, hash2);
        assertNotNull(hash1);
        assertTrue(hash1.length() > 0);
    }

    @Test
    void testComputeInputHash_DifferentDungeons_DifferentHashes() {
        // Given
        int[][] dungeon1 = {{1, 2}, {3, 4}};
        int[][] dungeon2 = {{4, 3}, {2, 1}};

        // When
        String hash1 = cacheService.computeInputHash(dungeon1);
        String hash2 = cacheService.computeInputHash(dungeon2);

        // Then
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testGetCachedSolution_Found() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.getDungeonSolution(expectedHash))
            .thenReturn(Optional.of(testSuccessResult));

        // When
        Optional<DungeonSolveResult> result = cacheService.getCachedSolution(testDungeon);

        // Then
        assertTrue(result.isPresent());
        assertInstanceOf(DungeonSolveResult.Success.class, result.get());

        DungeonSolveResult.Success success = (DungeonSolveResult.Success) result.get();
        assertEquals(7, success.minHp());
        assertEquals(3, success.path().size());

        verify(databaseMapper).getDungeonSolution(expectedHash);
    }

    @Test
    void testGetCachedSolution_NotFound() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.getDungeonSolution(expectedHash))
            .thenReturn(Optional.empty());

        // When
        Optional<DungeonSolveResult> result = cacheService.getCachedSolution(testDungeon);

        // Then
        assertFalse(result.isPresent());
        verify(databaseMapper).getDungeonSolution(expectedHash);
    }

    @Test
    void testCacheSolution_Success() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.solutionExists(expectedHash)).thenReturn(false);

        // When
        cacheService.cacheSolution(testDungeon, testSuccessResult);

        // Then
        verify(databaseMapper).solutionExists(expectedHash);
        verify(databaseMapper).saveDungeonWithSolution(testDungeon, expectedHash, testSuccessResult);
    }

    @Test
    void testCacheSolution_AlreadyExists() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.solutionExists(expectedHash)).thenReturn(true);

        // When
        cacheService.cacheSolution(testDungeon, testSuccessResult);

        // Then
        verify(databaseMapper).solutionExists(expectedHash);
        verify(databaseMapper, never()).saveDungeonWithSolution(any(), any(), any());
    }

    @Test
    void testCacheSolution_Failure_NotCached() {
        // Given & When
        cacheService.cacheSolution(testDungeon, testFailureResult);

        // Then
        verify(databaseMapper, never()).solutionExists(any());
        verify(databaseMapper, never()).saveDungeonWithSolution(any(), any(), any());
    }

    @Test
    void testIsCached_True() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.solutionExists(expectedHash)).thenReturn(true);

        // When
        boolean result = cacheService.isCached(testDungeon);

        // Then
        assertTrue(result);
        verify(databaseMapper).solutionExists(expectedHash);
    }

    @Test
    void testIsCached_False() {
        // Given
        String expectedHash = cacheService.computeInputHash(testDungeon);
        when(databaseMapper.solutionExists(expectedHash)).thenReturn(false);

        // When
        boolean result = cacheService.isCached(testDungeon);

        // Then
        assertFalse(result);
        verify(databaseMapper).solutionExists(expectedHash);
    }

    @Test
    void testGetCacheStatistics() {
        // Given
        String expectedStats = "Database Statistics: 5 dungeons, Avg dimensions: 3.2x4.1, Row range: 2-5, Col range: 3-6";
        when(databaseMapper.getDatabaseStatistics()).thenReturn(expectedStats);

        // When
        String result = cacheService.getCacheStatistics();

        // Then
        assertEquals(expectedStats, result);
        verify(databaseMapper).getDatabaseStatistics();
    }

    @Test
    void testClearCache() {
        // Given
        long expectedCount = 10L;
        when(databaseMapper.clearAllData()).thenReturn(expectedCount);

        // When
        long result = cacheService.clearCache();

        // Then
        assertEquals(expectedCount, result);
        verify(databaseMapper).clearAllData();
    }

    @Test
    void testComputeInputHash_NullDungeon_ThrowsException() {
        // Given & When & Then
        assertThrows(RuntimeException.class, () -> {
            cacheService.computeInputHash(null);
        });
    }

    @Test
    void testComputeInputHash_EmptyDungeon_ThrowsException() {
        // Given
        int[][] emptyDungeon = new int[0][0];

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cacheService.computeInputHash(emptyDungeon);
        });
    }
}
