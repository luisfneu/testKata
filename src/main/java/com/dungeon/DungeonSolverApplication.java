package com.dungeon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Dungeon Solver system.
 * 
 * This application provides a REST API to solve dungeon traversal problems,
 * calculating the minimum initial health required for a knight to reach
 * the princess while navigating through a grid of rooms with various effects.
 */
@SpringBootApplication
public class DungeonSolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(DungeonSolverApplication.class, args);
    }
}
