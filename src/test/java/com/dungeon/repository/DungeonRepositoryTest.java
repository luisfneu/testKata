package com.dungeon.repository;

import com.dungeon.entity.Dungeon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DungeonRepository.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class DungeonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DungeonRepository dungeonRepository;

    private Dungeon testDungeon;

    @BeforeEach
    void setUp() {
        testDungeon = new Dungeon();
        testDungeon.setHashInput("test-hash-123");
        testDungeon.setRows(3);
        testDungeon.setCols(3);
        testDungeon.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSaveAndFindById() {
        // When
        Dungeon saved = dungeonRepository.save(testDungeon);
        Optional<Dungeon> found = dungeonRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(testDungeon.getHashInput(), found.get().getHashInput());
        assertEquals(testDungeon.getRows(), found.get().getRows());
        assertEquals(testDungeon.getCols(), found.get().getCols());
    }

    @Test
    void testFindByHashInput() {
        // Given
        dungeonRepository.save(testDungeon);

        // When
        Optional<Dungeon> found = dungeonRepository.findByHashInput("test-hash-123");

        // Then
        assertTrue(found.isPresent());
        assertEquals(testDungeon.getHashInput(), found.get().getHashInput());
    }

    @Test
    void testFindByHashInput_NotFound() {
        // When
        Optional<Dungeon> found = dungeonRepository.findByHashInput("non-existent-hash");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByHashInput() {
        // Given
        dungeonRepository.save(testDungeon);

        // When
        boolean exists = dungeonRepository.existsByHashInput("test-hash-123");
        boolean notExists = dungeonRepository.existsByHashInput("non-existent-hash");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testGetStatistics() {
        // Given
        Dungeon dungeon1 = new Dungeon("hash1", 2, 3);
        Dungeon dungeon2 = new Dungeon("hash2", 4, 5);
        Dungeon dungeon3 = new Dungeon("hash3", 3, 4);

        dungeonRepository.save(dungeon1);
        dungeonRepository.save(dungeon2);
        dungeonRepository.save(dungeon3);

        // When
        Object[] stats = dungeonRepository.getStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(7, stats.length);

        // Count should be 3
        assertEquals(3L, ((Number) stats[0]).longValue());

        // Average rows should be 3.0 (2+4+3)/3
        assertEquals(3.0, ((Number) stats[1]).doubleValue(), 0.1);

        // Average cols should be 4.0 (3+5+4)/3
        assertEquals(4.0, ((Number) stats[2]).doubleValue(), 0.1);

        // Min rows should be 2
        assertEquals(2, ((Number) stats[3]).intValue());

        // Max rows should be 4
        assertEquals(4, ((Number) stats[4]).intValue());

        // Min cols should be 3
        assertEquals(3, ((Number) stats[5]).intValue());

        // Max cols should be 5
        assertEquals(5, ((Number) stats[6]).intValue());
    }

    @Test
    void testGetStatistics_EmptyDatabase() {
        // When
        Object[] stats = dungeonRepository.getStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(7, stats.length);
        assertEquals(0L, ((Number) stats[0]).longValue());
    }

    @Test
    void testUniqueHashInput() {
        // Given
        dungeonRepository.save(testDungeon);

        // When & Then
        Dungeon duplicateHash = new Dungeon();
        duplicateHash.setHashInput("test-hash-123");
        duplicateHash.setRows(5);
        duplicateHash.setCols(5);
        duplicateHash.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            dungeonRepository.save(duplicateHash);
            entityManager.flush();
        });
    }
}
