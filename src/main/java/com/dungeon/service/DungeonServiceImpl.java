package com.dungeon.service;

import com.dungeon.dto.DungeonRequest;
import com.dungeon.dto.DungeonResponse;
import com.dungeon.model.DungeonSolveResult;
import com.dungeon.solver.DungeonSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of DungeonService following hexagonal architecture principles.
 * 
 * This service acts as the application layer, orchestrating the interaction
 * between the domain logic (solver), the caching layer, and the infrastructure layer (controllers).
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Handles dungeon solving business logic with caching
 * - Open/Closed: Open for extension through dependency injection
 * - Liskov Substitution: Can be substituted for any DungeonService implementation
 * - Interface Segregation: Implements only necessary service methods
 * - Dependency Inversion: Depends on DungeonSolver and DungeonCacheService abstractions
 */
@Service
public class DungeonServiceImpl implements DungeonService {
    
    private static final Logger logger = LoggerFactory.getLogger(DungeonServiceImpl.class);
    
    private final DungeonSolver dungeonSolver;
    private final DungeonCacheService cacheService;
    
    /**
     * Constructor injection for dependency inversion.
     * 
     * @param dungeonSolver the solver implementation to use
     * @param cacheService the cache service for storing/retrieving solutions
     */
    @Autowired
    public DungeonServiceImpl(DungeonSolver dungeonSolver, DungeonCacheService cacheService) {
        this.dungeonSolver = dungeonSolver;
        this.cacheService = cacheService;
    }
    
    @Override
    public DungeonResponse solveDungeon(DungeonRequest request) throws DungeonSolvingException {
        logger.info("Solving dungeon with dimensions: {}x{}", 
                   request.getRows(), request.getCols());
        
        try {
            DungeonSolveResult result = processDungeonSolving(request.input());
            
            return switch (result) {
                case DungeonSolveResult.Success success -> {
                    logger.info("Dungeon solved successfully. Minimum HP: {}, Path length: {}", 
                               success.minHp(), success.path().size());
                    yield DungeonResponse.fromPositions(success.input(), success.path(), success.minHp());
                }
                case DungeonSolveResult.Failure failure -> {
                    logger.warn("Dungeon solving failed: {} ({})", failure.reason(), failure.errorCode());
                    throw new DungeonSolvingException(failure.reason(), failure.errorCode().name(), failure.input());
                }
            };
            
        } catch (DungeonSolvingException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during dungeon solving", e);
            throw new DungeonSolvingException(
                "An unexpected error occurred while solving the dungeon: " + e.getMessage(),
                "PROCESSING_ERROR",
                e
            );
        }
    }
    
    @Override
    public DungeonSolveResult processDungeonSolving(int[][] dungeonGrid) {
        if (dungeonGrid == null) {
            return new DungeonSolveResult.Failure(
                null,
                "Dungeon grid cannot be null",
                DungeonSolveResult.ErrorCode.INVALID_INPUT
            );
        }
        
        logger.debug("Processing dungeon grid with {} rows and {} columns", 
                    dungeonGrid.length, 
                    dungeonGrid.length > 0 ? dungeonGrid[0].length : 0);
        
        try {
            // First, check if we have a cached solution
            Optional<DungeonSolveResult> cachedResult = cacheService.getCachedSolution(dungeonGrid);
            if (cachedResult.isPresent()) {
                logger.info("Found cached solution for dungeon ({}x{})", 
                           dungeonGrid.length, dungeonGrid[0].length);
                return cachedResult.get();
            }
            
            // No cached solution, solve using the algorithm
            logger.debug("No cached solution found, computing new solution");
            DungeonSolveResult result = dungeonSolver.solve(dungeonGrid);
            
            // Log and cache the result
            switch (result) {
                case DungeonSolveResult.Success success -> {
                    logger.debug("Solver found solution with minimum HP: {}", success.minHp());
                    // Cache the successful solution for future use
                    cacheService.cacheSolution(dungeonGrid, success);
                    logger.debug("Solution cached for future lookups");
                }
                case DungeonSolveResult.Failure failure -> {
                    logger.debug("Solver failed: {} ({})", failure.reason(), failure.errorCode());
                    // We could cache failures too, but for now we'll only cache successes
                    // to avoid caching temporary issues like invalid input
                }
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in solver processing", e);
            return new DungeonSolveResult.Failure(
                dungeonGrid,
                "Solver processing error: " + e.getMessage(),
                DungeonSolveResult.ErrorCode.PROCESSING_ERROR
            );
        }
    }
}
