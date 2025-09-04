package com.dungeon.controller;

import com.dungeon.dto.DungeonRequest;
import com.dungeon.dto.DungeonResponse;
import com.dungeon.dto.ErrorResponse;
import com.dungeon.service.DungeonService;
import com.dungeon.service.DungeonSolvingException;
import com.dungeon.service.DungeonCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST Controller for dungeon solving operations.
 * 
 * This controller represents the infrastructure layer in hexagonal architecture,
 * providing the HTTP interface to the dungeon solving domain.
 * 
 * Follows REST principles and provides comprehensive OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/dungeon")
@Tag(name = "Dungeon Solver", description = "API for solving dungeon traversal problems")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class DungeonController {
    
    private static final Logger logger = LoggerFactory.getLogger(DungeonController.class);
    
    private final DungeonService dungeonService;
    private final DungeonCacheService cacheService;
    
    /**
     * Constructor injection for dependency inversion.
     * 
     * @param dungeonService the service to handle dungeon solving logic
     * @param cacheService the service to handle cache operations
     */
    @Autowired
    public DungeonController(DungeonService dungeonService, DungeonCacheService cacheService) {
        this.dungeonService = dungeonService;
        this.cacheService = cacheService;
    }
    
    /**
     * Solves a dungeon traversal problem.
     * 
     * The knight starts at the top-left cell [0,0] and must reach the bottom-right
     * cell [m-1,n-1], moving only right or down. Each cell contains a value that
     * affects the knight's health: negative values cause damage, positive values
     * provide healing, and zero is neutral.
     * 
     * @param request the dungeon request containing the grid to solve
     * @param bindingResult validation results
     * @return ResponseEntity containing the solution or error information
     */
    @PostMapping("/solve")
    @Operation(
        summary = "Solve dungeon traversal problem",
        description = "Calculates the minimum initial health required for a knight to traverse " +
                     "from top-left to bottom-right of a dungeon grid, moving only right or down. " +
                     "Returns the optimal path and minimum health points needed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dungeon solved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DungeonResponse.class),
                examples = @ExampleObject(
                    name = "Successful solution",
                    value = """
                        {
                          "input": [[-2, -3, 3], [-5, -10, 1], [10, 30, -5]],
                          "path": [[0,0], [0,1], [0,2], [1,2], [2,2]],
                          "min_hp": 7
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or unsolvable dungeon",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Validation error",
                    value = """
                        {
                          "status": 400,
                          "message": "Invalid dungeon: must not be empty",
                          "errorCode": "INVALID_INPUT",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "path": "/api/dungeon/solve"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Dungeon is unsolvable",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<?> solveDungeon(
        @Parameter(
            description = "Dungeon grid to solve",
            required = true,
            schema = @Schema(implementation = DungeonRequest.class)
        )
        @Valid @RequestBody DungeonRequest request,
        BindingResult bindingResult
    ) {
        logger.info("Received dungeon solve request with {}x{} grid", 
                   request.getRows(), request.getCols());
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            logger.warn("Validation failed: {}", errorMessage);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                "INVALID_INPUT",
                Instant.now().toString(),
                "/api/dungeon/solve"
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            DungeonResponse response = dungeonService.solveDungeon(request);
            logger.info("Dungeon solved successfully. Min HP: {}, Path length: {}", 
                       response.min_hp(), response.path().length);
            return ResponseEntity.ok(response);
            
        } catch (DungeonSolvingException e) {
            logger.warn("Dungeon solving failed: {}", e.getMessage());
            
            HttpStatus status = determineHttpStatus(e.getErrorCode());
            ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                e.getMessage(),
                e.getErrorCode(),
                Instant.now().toString(),
                "/api/dungeon/solve"
            );
            
            return ResponseEntity.status(status).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during dungeon solving", e);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred while processing your request",
                "PROCESSING_ERROR",
                Instant.now().toString(),
                "/api/dungeon/solve"
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint.
     * 
     * @return simple health check response
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Simple health check endpoint to verify the service is running"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service is healthy",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                      "status": "UP",
                      "service": "Dungeon Solver API",
                      "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """
            )
        )
    )
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(java.util.Map.of(
            "status", "UP",
            "service", "Dungeon Solver API",
            "timestamp", Instant.now().toString()
        ));
    }
    
    /**
     * Gets cache statistics.
     */
    @GetMapping("/cache/stats")
    @Operation(
        summary = "Get cache statistics",
        description = "Returns statistics about cached dungeon solutions"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cache statistics retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                      "statistics": "Cache Statistics: 42 solutions cached, Avg MinHP: 15.75, Range: 1-50",
                      "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """
            )
        )
    )
    public ResponseEntity<?> getCacheStatistics() {
        logger.info("Cache statistics requested");
        
        String stats = cacheService.getCacheStatistics();
        
        return ResponseEntity.ok(java.util.Map.of(
            "statistics", stats,
            "timestamp", Instant.now().toString()
        ));
    }
    
    /**
     * Clears the solution cache.
     */
    @DeleteMapping("/cache")
    @Operation(
        summary = "Clear solution cache",
        description = "Removes all cached dungeon solutions"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cache cleared successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                      "message": "Cache cleared successfully",
                      "solutionsRemoved": 42,
                      "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """
            )
        )
    )
    public ResponseEntity<?> clearCache() {
        logger.info("Cache clear requested");
        
        long removedCount = cacheService.clearCache();
        
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Cache cleared successfully",
            "solutionsRemoved", removedCount,
            "timestamp", Instant.now().toString()
        ));
    }
    
    /**
     * Checks if a dungeon solution is cached.
     */
    @PostMapping("/cache/check")
    @Operation(
        summary = "Check if solution is cached",
        description = "Checks whether a solution for the given dungeon is already cached"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cache check completed",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                      "cached": true,
                      "hash": "a1b2c3d4e5f6...",
                      "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """
            )
        )
    )
    public ResponseEntity<?> checkCached(
        @Parameter(description = "Dungeon grid to check", required = true)
        @Valid @RequestBody DungeonRequest request
    ) {
        logger.info("Cache check requested for {}x{} dungeon", 
                   request.getRows(), request.getCols());
        
        boolean isCached = cacheService.isCached(request.input());
        String hash = cacheService.computeInputHash(request.input());
        
        return ResponseEntity.ok(java.util.Map.of(
            "cached", isCached,
            "hash", hash,
            "timestamp", Instant.now().toString()
        ));
    }
    
    /**
     * Determines the appropriate HTTP status based on the error code.
     * 
     * @param errorCode the error code from the exception
     * @return appropriate HTTP status
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "INVALID_INPUT" -> HttpStatus.BAD_REQUEST;
            case "UNSOLVABLE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "INFINITE_HP" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "PROCESSING_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
