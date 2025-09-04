package com.dungeon.repository;

import com.dungeon.entity.SolutionPath;
import com.dungeon.entity.SolutionPathId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SolutionPath entity operations.
 */
@Repository
public interface SolutionPathRepository extends JpaRepository<SolutionPath, SolutionPathId> {

    /**
     * Find all solution path steps for a given dungeon ordered by position.
     *
     * @param dungeonId the dungeon ID
     * @return list of solution path steps ordered by position
     */
    @Query("SELECT sp FROM SolutionPath sp WHERE sp.dungeon.id = :dungeonId ORDER BY sp.position ASC")
    List<SolutionPath> findByDungeonIdOrderByPositionAsc(@Param("dungeonId") UUID dungeonId);

    /**
     * Check if a solution exists for a given dungeon.
     *
     * @param dungeonId the dungeon ID
     * @return true if solution exists, false otherwise
     */
    @Query("SELECT COUNT(sp) > 0 FROM SolutionPath sp WHERE sp.dungeon.id = :dungeonId")
    boolean existsByDungeonId(@Param("dungeonId") UUID dungeonId);

    /**
     * Get the minimum HP for a dungeon solution.
     *
     * @param dungeonId the dungeon ID
     * @return the minimum HP required, or null if no solution exists
     */
    @Query("SELECT sp.minHp FROM SolutionPath sp WHERE sp.dungeon.id = :dungeonId AND sp.position = 0")
    Integer getMinHpByDungeonId(@Param("dungeonId") UUID dungeonId);

    /**
     * Delete all solution paths for a given dungeon.
     *
     * @param dungeonId the dungeon ID
     */
    @Query("DELETE FROM SolutionPath sp WHERE sp.dungeon.id = :dungeonId")
    void deleteByDungeonId(@Param("dungeonId") UUID dungeonId);
}
