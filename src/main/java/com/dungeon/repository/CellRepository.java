package com.dungeon.repository;

import com.dungeon.entity.Cell;
import com.dungeon.entity.Dungeon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Cell entity operations.
 */
@Repository
public interface CellRepository extends JpaRepository<Cell, UUID> {

    /**
     * Find all cells for a given dungeon.
     *
     * @param dungeon the dungeon entity
     * @return list of cells ordered by row and column
     */
    @Query("SELECT c FROM Cell c WHERE c.dungeon = :dungeon ORDER BY c.rowIndex, c.colIndex")
    List<Cell> findByDungeonOrderByRowIndexAndColIndex(@Param("dungeon") Dungeon dungeon);

    /**
     * Find a specific cell by dungeon, row and column indices.
     *
     * @param dungeon the dungeon entity
     * @param rowIndex the row index
     * @param colIndex the column index
     * @return Optional containing the cell if found
     */
    Optional<Cell> findByDungeonAndRowIndexAndColIndex(Dungeon dungeon, Integer rowIndex, Integer colIndex);

    /**
     * Find all cells for a dungeon by dungeon ID.
     *
     * @param dungeonId the dungeon ID
     * @return list of cells ordered by row and column
     */
    @Query("SELECT c FROM Cell c WHERE c.dungeon.id = :dungeonId ORDER BY c.rowIndex, c.colIndex")
    List<Cell> findByDungeonIdOrderByRowIndexAndColIndex(@Param("dungeonId") UUID dungeonId);
}
