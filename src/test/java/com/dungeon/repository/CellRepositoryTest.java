package com.dungeon.repository;

import com.dungeon.entity.Cell;
import com.dungeon.entity.Dungeon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CellRepository.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CellRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CellRepository cellRepository;

    @Autowired
    private DungeonRepository dungeonRepository;

    private Dungeon testDungeon;
    private Cell cell1, cell2, cell3, cell4;

    @BeforeEach
    void setUp() {
        testDungeon = new Dungeon();
        testDungeon.setHashInput("test-hash-cells");
        testDungeon.setRows(2);
        testDungeon.setCols(2);
        testDungeon.setCreatedAt(LocalDateTime.now());
        testDungeon = dungeonRepository.save(testDungeon);

        // Create 2x2 dungeon cells
        cell1 = new Cell(testDungeon, 0, 0, -3);
        cell2 = new Cell(testDungeon, 0, 1, 5);
        cell3 = new Cell(testDungeon, 1, 0, 1);
        cell4 = new Cell(testDungeon, 1, 1, -4);
    }

    @Test
    void testSaveAndFindById() {
        // When
        Cell saved = cellRepository.save(cell1);
        Optional<Cell> found = cellRepository.findById(saved.getCellId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(cell1.getRowIndex(), found.get().getRowIndex());
        assertEquals(cell1.getColIndex(), found.get().getColIndex());
        assertEquals(cell1.getValue(), found.get().getValue());
    }

    @Test
    void testFindByDungeonOrderByRowIndexAndColIndex() {
        // Given
        cellRepository.save(cell4); // Save in reverse order
        cellRepository.save(cell3);
        cellRepository.save(cell2);
        cellRepository.save(cell1);

        // When
        List<Cell> cells = cellRepository.findByDungeonOrderByRowIndexAndColIndex(testDungeon);

        // Then
        assertEquals(4, cells.size());

        // Should be ordered by row, then col
        assertEquals(0, cells.get(0).getRowIndex());
        assertEquals(0, cells.get(0).getColIndex());
        assertEquals(-3, cells.get(0).getValue());

        assertEquals(0, cells.get(1).getRowIndex());
        assertEquals(1, cells.get(1).getColIndex());
        assertEquals(5, cells.get(1).getValue());

        assertEquals(1, cells.get(2).getRowIndex());
        assertEquals(0, cells.get(2).getColIndex());
        assertEquals(1, cells.get(2).getValue());

        assertEquals(1, cells.get(3).getRowIndex());
        assertEquals(1, cells.get(3).getColIndex());
        assertEquals(-4, cells.get(3).getValue());
    }

    @Test
    void testFindByDungeonAndRowIndexAndColIndex() {
        // Given
        cellRepository.save(cell1);

        // When
        Optional<Cell> found = cellRepository.findByDungeonAndRowIndexAndColIndex(testDungeon, 0, 0);
        Optional<Cell> notFound = cellRepository.findByDungeonAndRowIndexAndColIndex(testDungeon, 5, 5);

        // Then
        assertTrue(found.isPresent());
        assertEquals(-3, found.get().getValue());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByDungeonIdOrderByRowIndexAndColIndex() {
        // Given
        cellRepository.save(cell4);
        cellRepository.save(cell3);
        cellRepository.save(cell2);
        cellRepository.save(cell1);

        // When
        List<Cell> cells = cellRepository.findByDungeonIdOrderByRowIndexAndColIndex(testDungeon.getId());

        // Then
        assertEquals(4, cells.size());

        // Verify ordering
        assertEquals(0, cells.get(0).getRowIndex());
        assertEquals(0, cells.get(0).getColIndex());
        assertEquals(1, cells.get(3).getRowIndex());
        assertEquals(1, cells.get(3).getColIndex());
    }

    @Test
    void testUniqueConstraintOnDungeonRowCol() {
        // Given
        cellRepository.save(cell1);

        // When & Then - try to save another cell with same dungeon, row, col
        Cell duplicate = new Cell(testDungeon, 0, 0, 999);

        assertThrows(Exception.class, () -> {
            cellRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void testCascadeDeleteWithDungeon() {
        // Given
        cellRepository.save(cell1);
        cellRepository.save(cell2);

        assertEquals(2, cellRepository.findByDungeonOrderByRowIndexAndColIndex(testDungeon).size());

        // When - delete dungeon
        dungeonRepository.delete(testDungeon);
        entityManager.flush();

        // Then - cells should be deleted due to cascade
        assertEquals(0, cellRepository.findByDungeonOrderByRowIndexAndColIndex(testDungeon).size());
    }
}
