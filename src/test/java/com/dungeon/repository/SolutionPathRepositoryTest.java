package com.dungeon.repository;

import com.dungeon.entity.Cell;
import com.dungeon.entity.Dungeon;
import com.dungeon.entity.SolutionPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SolutionPathRepository.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SolutionPathRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SolutionPathRepository solutionPathRepository;

    @Autowired
    private DungeonRepository dungeonRepository;

    @Autowired
    private CellRepository cellRepository;

    private Dungeon testDungeon;
    private Cell cell1, cell2, cell3;
    private SolutionPath path1, path2, path3;

    @BeforeEach
    void setUp() {
        // Create test dungeon
        testDungeon = new Dungeon();
        testDungeon.setHashInput("test-hash-solution");
        testDungeon.setRows(2);
        testDungeon.setCols(2);
        testDungeon.setCreatedAt(LocalDateTime.now());
        testDungeon = dungeonRepository.save(testDungeon);

        // Create test cells
        cell1 = new Cell(testDungeon, 0, 0, -3);
        cell2 = new Cell(testDungeon, 0, 1, 5);
        cell3 = new Cell(testDungeon, 1, 1, -4);

        cell1 = cellRepository.save(cell1);
        cell2 = cellRepository.save(cell2);
        cell3 = cellRepository.save(cell3);

        // Create solution path: cell1 -> cell2 -> cell3
        path1 = new SolutionPath(testDungeon, cell1, 0, 7);
        path2 = new SolutionPath(testDungeon, cell2, 1, 7);
        path3 = new SolutionPath(testDungeon, cell3, 2, 7);
    }

    @Test
    void testSaveAndFindByDungeonIdOrderByPositionAsc() {
        // Given - save in reverse order to test ordering
        solutionPathRepository.save(path3);
        solutionPathRepository.save(path1);
        solutionPathRepository.save(path2);

        // When
        List<SolutionPath> paths = solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId());

        // Then
        assertEquals(3, paths.size());

        // Should be ordered by position
        assertEquals(0, paths.get(0).getPosition());
        assertEquals(cell1.getCellId(), paths.get(0).getCell().getCellId());

        assertEquals(1, paths.get(1).getPosition());
        assertEquals(cell2.getCellId(), paths.get(1).getCell().getCellId());

        assertEquals(2, paths.get(2).getPosition());
        assertEquals(cell3.getCellId(), paths.get(2).getCell().getCellId());

        // All should have same minHp
        paths.forEach(path -> assertEquals(7, path.getMinHp()));
    }

    @Test
    void testExistsByDungeonId() {
        // Given
        solutionPathRepository.save(path1);

        // When
        boolean exists = solutionPathRepository.existsByDungeonId(testDungeon.getId());

        // Create another dungeon without solution
        Dungeon emptyDungeon = new Dungeon();
        emptyDungeon.setHashInput("empty-dungeon");
        emptyDungeon.setRows(1);
        emptyDungeon.setCols(1);
        emptyDungeon.setCreatedAt(LocalDateTime.now());
        emptyDungeon = dungeonRepository.save(emptyDungeon);

        boolean notExists = solutionPathRepository.existsByDungeonId(emptyDungeon.getId());

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testGetMinHpByDungeonId() {
        // Given
        solutionPathRepository.save(path1); // position 0
        solutionPathRepository.save(path2); // position 1

        // When
        Integer minHp = solutionPathRepository.getMinHpByDungeonId(testDungeon.getId());

        // Then
        assertNotNull(minHp);
        assertEquals(7, minHp);
    }

    @Test
    void testGetMinHpByDungeonId_NoSolution() {
        // Given - no solution paths saved

        // When
        Integer minHp = solutionPathRepository.getMinHpByDungeonId(testDungeon.getId());

        // Then
        assertNull(minHp);
    }

    @Test
    void testDeleteByDungeonId() {
        // Given
        solutionPathRepository.save(path1);
        solutionPathRepository.save(path2);
        solutionPathRepository.save(path3);

        assertEquals(3, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());

        // When
        solutionPathRepository.deleteByDungeonId(testDungeon.getId());
        entityManager.flush();

        // Then
        assertEquals(0, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());
    }

    @Test
    void testCascadeDeleteWithDungeon() {
        // Given
        solutionPathRepository.save(path1);
        solutionPathRepository.save(path2);

        assertEquals(2, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());

        // When - delete dungeon
        dungeonRepository.delete(testDungeon);
        entityManager.flush();

        // Then - solution paths should be deleted due to cascade
        assertEquals(0, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());
    }

    @Test
    void testCascadeDeleteWithCell() {
        // Given
        solutionPathRepository.save(path1);

        assertEquals(1, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());

        // When - delete cell
        cellRepository.delete(cell1);
        entityManager.flush();

        // Then - solution path should be deleted due to cascade
        assertEquals(0, solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId()).size());
    }

    @Test
    void testMultipleDungeonsWithSolutions() {
        // Given - create another dungeon with solution
        Dungeon dungeon2 = new Dungeon();
        dungeon2.setHashInput("test-hash-2");
        dungeon2.setRows(1);
        dungeon2.setCols(1);
        dungeon2.setCreatedAt(LocalDateTime.now());
        dungeon2 = dungeonRepository.save(dungeon2);

        Cell cell2_1 = new Cell(dungeon2, 0, 0, 10);
        cell2_1 = cellRepository.save(cell2_1);

        SolutionPath path2_1 = new SolutionPath(dungeon2, cell2_1, 0, 1);

        // Save solutions for both dungeons
        solutionPathRepository.save(path1);
        solutionPathRepository.save(path2);
        solutionPathRepository.save(path2_1);

        // When
        List<SolutionPath> paths1 = solutionPathRepository.findByDungeonIdOrderByPositionAsc(testDungeon.getId());
        List<SolutionPath> paths2 = solutionPathRepository.findByDungeonIdOrderByPositionAsc(dungeon2.getId());

        // Then
        assertEquals(2, paths1.size());
        assertEquals(1, paths2.size());
        assertEquals(7, paths1.get(0).getMinHp());
        assertEquals(1, paths2.get(0).getMinHp());
    }
}
