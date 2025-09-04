package com.dungeon.service;

import com.dungeon.entity.Cell;
import com.dungeon.entity.Dungeon;
import com.dungeon.entity.SolutionPath;
import com.dungeon.model.DungeonSolveResult;
import com.dungeon.model.Position;
import com.dungeon.repository.CellRepository;
import com.dungeon.repository.DungeonRepository;
import com.dungeon.repository.SolutionPathRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for mapping between database entities and domain models.
 * Handles the conversion of dungeon data to/from database format.
 */
@Service
@Transactional
public class DungeonDatabaseMapper {

    private static final Logger logger = LoggerFactory.getLogger(DungeonDatabaseMapper.class);

    private final DungeonRepository dungeonRepository;
    private final CellRepository cellRepository;
    private final SolutionPathRepository solutionPathRepository;

    @Autowired
    public DungeonDatabaseMapper(
            DungeonRepository dungeonRepository,
            CellRepository cellRepository,
            SolutionPathRepository solutionPathRepository) {
        this.dungeonRepository = dungeonRepository;
        this.cellRepository = cellRepository;
        this.solutionPathRepository = solutionPathRepository;
    }

    /**
     * Saves a dungeon and its solution to the database.
     *
     * @param dungeonArray 2D array representing the dungeon
     * @param hashInput Blake3 hash of the dungeon input
     * @param solutionResult the solution result to save
     * @return the saved dungeon entity
     */
    public Dungeon saveDungeonWithSolution(int[][] dungeonArray, String hashInput, DungeonSolveResult.Success solutionResult) {
        logger.debug("Saving dungeon with hash: {}", hashInput);

        // Create and save dungeon entity
        Dungeon dungeon = new Dungeon();
        dungeon.setHashInput(hashInput);
        dungeon.setRows(dungeonArray.length);
        dungeon.setCols(dungeonArray[0].length);
        dungeon.setCreatedAt(LocalDateTime.now());

        dungeon = dungeonRepository.save(dungeon);
        logger.debug("Saved dungeon with ID: {}", dungeon.getId());

        // Create and save cell entities
        List<Cell> cells = new ArrayList<>();
        for (int row = 0; row < dungeonArray.length; row++) {
            for (int col = 0; col < dungeonArray[row].length; col++) {
                Cell cell = new Cell();
                cell.setDungeon(dungeon);
                cell.setRowIndex(row);
                cell.setColIndex(col);
                cell.setValue(dungeonArray[row][col]);
                cells.add(cell);
            }
        }

        cells = cellRepository.saveAll(cells);
        logger.debug("Saved {} cells for dungeon", cells.size());

        // Create and save solution path entities
        List<SolutionPath> solutionPaths = new ArrayList<>();
        List<Position> path = solutionResult.path();

        for (int position = 0; position < path.size(); position++) {
            Position pos = path.get(position);

            // Find the corresponding cell
            Cell pathCell = cells.stream()
                    .filter(cell -> cell.getRowIndex().equals(pos.row()) && cell.getColIndex().equals(pos.col()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cell not found for position: " + pos));

            SolutionPath solutionPath = new SolutionPath();
            solutionPath.setDungeon(dungeon);
            solutionPath.setCell(pathCell);
            solutionPath.setPosition(position);
            solutionPath.setMinHp(solutionResult.minHp());

            solutionPaths.add(solutionPath);
        }

        solutionPathRepository.saveAll(solutionPaths);
        logger.info("Saved solution path with {} steps for dungeon {}", solutionPaths.size(), dungeon.getId());

        return dungeon;
    }

    /**
     * Retrieves a dungeon solution from the database by hash input.
     *
     * @param hashInput Blake3 hash of the dungeon input
     * @return Optional containing the solution result if found
     */
    @Transactional(readOnly = true)
    public Optional<DungeonSolveResult.Success> getDungeonSolution(String hashInput) {
        logger.debug("Looking for dungeon solution with hash: {}", hashInput);

        Optional<Dungeon> dungeonOpt = dungeonRepository.findByHashInput(hashInput);

        if (dungeonOpt.isEmpty()) {
            logger.debug("No dungeon found for hash: {}", hashInput);
            return Optional.empty();
        }

        Dungeon dungeon = dungeonOpt.get();

        // Get all cells and reconstruct the dungeon array
        List<Cell> cells = cellRepository.findByDungeonOrderByRowIndexAndColIndex(dungeon);
        int[][] dungeonArray = reconstructDungeonArray(cells, dungeon.getRows(), dungeon.getCols());

        // Get solution path
        List<SolutionPath> solutionPaths = solutionPathRepository.findByDungeonIdOrderByPositionAsc(dungeon.getId());

        if (solutionPaths.isEmpty()) {
            logger.warn("No solution path found for dungeon: {}", dungeon.getId());
            return Optional.empty();
        }

        // Convert solution paths to positions
        List<Position> path = solutionPaths.stream()
                .map(sp -> new Position(sp.getCell().getRowIndex(), sp.getCell().getColIndex()))
                .toList();

        Integer minHp = solutionPaths.get(0).getMinHp(); // All should have the same minHp

        DungeonSolveResult.Success result = new DungeonSolveResult.Success(dungeonArray, path, minHp);

        logger.info("Retrieved solution for hash: {} with {} steps and minHP: {}", hashInput, path.size(), minHp);
        return Optional.of(result);
    }

    /**
     * Checks if a dungeon solution exists in the database.
     *
     * @param hashInput Blake3 hash of the dungeon input
     * @return true if solution exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean solutionExists(String hashInput) {
        return dungeonRepository.existsByHashInput(hashInput);
    }

    /**
     * Reconstructs a 2D dungeon array from cell entities.
     *
     * @param cells list of cell entities
     * @param rows number of rows
     * @param cols number of columns
     * @return 2D array representing the dungeon
     */
    private int[][] reconstructDungeonArray(List<Cell> cells, int rows, int cols) {
        int[][] dungeonArray = new int[rows][cols];

        for (Cell cell : cells) {
            dungeonArray[cell.getRowIndex()][cell.getColIndex()] = cell.getValue();
        }

        return dungeonArray;
    }

    /**
     * Gets database statistics.
     *
     * @return formatted statistics string
     */
    @Transactional(readOnly = true)
    public String getDatabaseStatistics() {
        Object[] stats = dungeonRepository.getStatistics();

        if (stats[0] == null) {
            return "Database is empty";
        }

        long dungeonCount = ((Number) stats[0]).longValue();
        double avgRows = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        double avgCols = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
        int minRows = stats[3] != null ? ((Number) stats[3]).intValue() : 0;
        int maxRows = stats[4] != null ? ((Number) stats[4]).intValue() : 0;
        int minCols = stats[5] != null ? ((Number) stats[5]).intValue() : 0;
        int maxCols = stats[6] != null ? ((Number) stats[6]).intValue() : 0;

        return String.format(
            "Database Statistics: %d dungeons, Avg dimensions: %.1fx%.1f, Row range: %d-%d, Col range: %d-%d",
            dungeonCount, avgRows, avgCols, minRows, maxRows, minCols, maxCols
        );
    }

    /**
     * Clears all dungeon data from the database.
     *
     * @return number of dungeons cleared
     */
    public long clearAllData() {
        long count = dungeonRepository.count();
        solutionPathRepository.deleteAll();
        cellRepository.deleteAll();
        dungeonRepository.deleteAll();
        logger.info("Cleared all database data: {} dungeons", count);
        return count;
    }
}
