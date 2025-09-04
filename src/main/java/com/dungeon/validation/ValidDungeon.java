package com.dungeon.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating dungeon grids.
 * Ensures the dungeon is not empty, has valid dimensions, and contains values within constraints.
 */
@Documented
@Constraint(validatedBy = DungeonValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDungeon {
    
    /**
     * Default validation error message.
     * 
     * @return error message
     */
    String message() default "Invalid dungeon configuration";
    
    /**
     * Validation groups.
     * 
     * @return validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for additional metadata.
     * 
     * @return payload classes
     */
    Class<? extends Payload>[] payload() default {};
}
