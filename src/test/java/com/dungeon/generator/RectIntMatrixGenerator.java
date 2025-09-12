package com.dungeon.generators;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

/**
 * Generates rectangular int[][] matrices with consistent row lengths and
 * values within the validator's accepted range. Dimensions are bounded to
 * keep payload sizes reasonable during fuzzing.
 */
public class RectIntMatrixGenerator extends Generator<int[][]> {

    // Keep dimensions within validator limits; smaller upper bound for perf
    private static final int MIN_DIM = 1;
    private static final int MAX_DIM = 50; // validator allows up to 200

    // Cell values per DungeonValidator
    private static final int MIN_VAL = -1000;
    private static final int MAX_VAL = 1000;

    public RectIntMatrixGenerator() {
        super(int[][].class);
    }

    @Override
    public int[][] generate(SourceOfRandomness r, GenerationStatus status) {
        int rows = r.nextInt(MIN_DIM, MAX_DIM);
        int cols = r.nextInt(MIN_DIM, MAX_DIM);

        int[][] grid = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = r.nextInt(MIN_VAL, MAX_VAL);
            }
        }
        return grid;
    }
}
