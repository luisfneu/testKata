package com.dungeon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the @ValidDungeon annotation.
 * Validates dungeon grids according to business rules and constraints.
 */
public class DungeonValidator implements ConstraintValidator<ValidDungeon, int[][]> {
    
    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 200;
    private static final int MIN_CELL_VALUE = -1000;
    private static final int MAX_CELL_VALUE = 1000;
    
    @Override
    public void initialize(ValidDungeon constraintAnnotation) {}
    
    @Override
    public boolean isValid(int[][] dungeon, ConstraintValidatorContext context) {
        if (dungeon == null) {
            addConstraintViolation(context, "Dungeon cannot be null");
            return false;
        }
        
        // Check if dungeon is empty
        if (dungeon.length == 0) {
            addConstraintViolation(context, "Dungeon cannot be empty");
            return false;
        }
        
        // Check row count constraints
        if (dungeon.length > MAX_DIMENSION) {
            addConstraintViolation(context, 
                String.format("Dungeon height cannot exceed %d rows", MAX_DIMENSION));
            return false;
        }
        
        // Check if first row is empty
        if (dungeon[0].length == 0) {
            addConstraintViolation(context, "Dungeon rows cannot be empty");
            return false;
        }
        
        int cols = dungeon[0].length;
        
        // Check column count constraints
        if (cols > MAX_DIMENSION) {
            addConstraintViolation(context, 
                String.format("Dungeon width cannot exceed %d columns", MAX_DIMENSION));
            return false;
        }
        
        // Validate each row
        for (int i = 0; i < dungeon.length; i++) {
            if (dungeon[i] == null) {
                addConstraintViolation(context, 
                    String.format("Row %d cannot be null", i));
                return false;
            }
            
            // Check consistent row length
            if (dungeon[i].length != cols) {
                addConstraintViolation(context, 
                    String.format("All rows must have the same length. Expected %d, but row %d has %d", 
                        cols, i, dungeon[i].length));
                return false;
            }
            
            // Validate cell values
            for (int j = 0; j < dungeon[i].length; j++) {
                int cellValue = dungeon[i][j];
                if (cellValue < MIN_CELL_VALUE || cellValue > MAX_CELL_VALUE) {
                    addConstraintViolation(context, 
                        String.format("Cell value at position [%d,%d] must be between %d and %d, but was %d", 
                            i, j, MIN_CELL_VALUE, MAX_CELL_VALUE, cellValue));
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Adds a custom constraint violation message.
     * 
     * @param context the validation context
     * @param message the custom message
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}
