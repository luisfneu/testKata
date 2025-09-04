package com.dungeon.service;

import com.dungeon.model.DungeonSolveResult;
import com.dungeon.model.Position;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing cached dungeon solutions in the database.
 * Handles Blake3 hashing and cache operations using the new multi-table structure.
 */
@Service
@Transactional
public class DungeonCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(DungeonCacheService.class);
    
    private final DungeonDatabaseMapper databaseMapper;

    static {
        // Add Bouncy Castle provider for Blake3 support
        Security.addProvider(new BouncyCastleProvider());
    }
    
    @Autowired
    public DungeonCacheService(DungeonDatabaseMapper databaseMapper) {
        this.databaseMapper = databaseMapper;
    }
    
    /**
     * Computes Blake3 hash of dungeon input.
     * Blake3 is a cryptographic hash function that's faster than SHA-2 and provides better security.
     * 
     * @param dungeon 2D array representing the dungeon
     * @return hex string of the hash
     */
    public String computeInputHash(int[][] dungeon) {
        try {
            // Try Blake3 first, fallback to SHA-256 if not available
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("BLAKE3-256", "BC");
            } catch (NoSuchAlgorithmException e) {
                logger.warn("Blake3 not available, falling back to SHA-256");
                digest = MessageDigest.getInstance("SHA-256");
            }
            
            // Convert dungeon to bytes for hashing
            ByteBuffer buffer = ByteBuffer.allocate(dungeon.length * dungeon[0].length * 4);
            for (int[] row : dungeon) {
                for (int cell : row) {
                    buffer.putInt(cell);
                }
            }
            
            byte[] hash = digest.digest(buffer.array());
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("Hash computation failed", e);
            throw new RuntimeException("Hash computation failed", e);
        }
    }
    
    /**
     * Retrieves a cached solution by dungeon input.
     * 
     * @param dungeon 2D array representing the dungeon
     * @return Optional containing the cached result if found
     */
    @Transactional(readOnly = true)
    public Optional<DungeonSolveResult> getCachedSolution(int[][] dungeon) {
        String inputHash = computeInputHash(dungeon);
        logger.debug("Looking for cached solution with hash: {}", inputHash);
        
        Optional<DungeonSolveResult.Success> cached = databaseMapper.getDungeonSolution(inputHash);

        if (cached.isPresent()) {
            logger.info("Found cached solution for hash: {}", inputHash);
            return Optional.of(cached.get());
        }
        
        logger.debug("No cached solution found for hash: {}", inputHash);
        return Optional.empty();
    }
    
    /**
     * Caches a solution result for future lookups.
     * 
     * @param dungeon 2D array representing the dungeon
     * @param result the solution result to cache
     */
    public void cacheSolution(int[][] dungeon, DungeonSolveResult result) {
        // Only cache successful results
        if (!(result instanceof DungeonSolveResult.Success success)) {
            logger.debug("Not caching failed result");
            return;
        }
        
        String inputHash = computeInputHash(dungeon);
        
        // Check if already cached to avoid duplicates
        if (databaseMapper.solutionExists(inputHash)) {
            logger.debug("Solution already cached for hash: {}", inputHash);
            return;
        }
        
        databaseMapper.saveDungeonWithSolution(dungeon, inputHash, success);
        logger.info("Cached solution for hash: {} ({}x{}, minHP: {})",
                   inputHash, dungeon.length, dungeon[0].length, success.minHp());
    }
    
    /**
     * Checks if a solution is cached for the given dungeon.
     * 
     * @param dungeon 2D array representing the dungeon
     * @return true if a solution is cached
     */
    @Transactional(readOnly = true)
    public boolean isCached(int[][] dungeon) {
        String inputHash = computeInputHash(dungeon);
        return databaseMapper.solutionExists(inputHash);
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return cache statistics as a formatted string
     */
    @Transactional(readOnly = true)
    public String getCacheStatistics() {
        return databaseMapper.getDatabaseStatistics();
    }
    
    /**
     * Clears all cached solutions.
     * 
     * @return number of solutions cleared
     */
    public long clearCache() {
        long count = databaseMapper.clearAllData();
        logger.info("Cleared {} cached solutions", count);
        return count;
    }
    
    /**
     * Converts byte array to hex string.
     * 
     * @param bytes byte array to convert
     * @return hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
