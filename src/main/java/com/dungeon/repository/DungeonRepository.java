package com.dungeon.repository;

import com.dungeon.entity.Dungeon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Dungeon entity operations.
 */
@Repository
public interface DungeonRepository extends JpaRepository<Dungeon, UUID> {

    /**
     * Find a dungeon by its input hash.
     *
     * @param hashInput the Blake3 hash of the dungeon input
     * @return Optional containing the dungeon if found
     */
    Optional<Dungeon> findByHashInput(String hashInput);

    /**
     * Check if a dungeon exists by its input hash.
     *
     * @param hashInput the Blake3 hash of the dungeon input
     * @return true if exists, false otherwise
     */
    boolean existsByHashInput(String hashInput);

    /**
     * Get cache statistics for dungeons.
     *
     * @return array containing [count, avg_rows, avg_cols, min_rows, max_rows, min_cols, max_cols]
     */
    @Query(value = """
        SELECT 
            COUNT(*) as count,
            AVG(rows) as avg_rows,
            AVG(cols) as avg_cols,
            MIN(rows) as min_rows,
            MAX(rows) as max_rows,
            MIN(cols) as min_cols,
            MAX(cols) as max_cols
        FROM dungeon
        """, nativeQuery = true)
    Object[] getStatistics();
}
