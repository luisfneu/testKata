package com.dungeon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for SpringDoc.
 * Configures Swagger UI and OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.application.name:Dungeon Solver}")
    private String applicationName;
    
    /**
     * Configures the OpenAPI specification.
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Dungeon Solver API")
                .description("""
                    A Java 23 Spring Boot REST API for solving dungeon traversal problems.
                    
                    ## Problem Description
                    
                    Given a 2D grid representing a dungeon, calculate the minimum initial health 
                    a knight needs to rescue a princess. The knight:
                    
                    - Starts at the top-left corner [0,0]
                    - Must reach the bottom-right corner [m-1,n-1]
                    - Can only move right or down
                    - Loses/gains health based on cell values:
                      - Negative values: damage from demons
                      - Positive values: healing from magic orbs
                      - Zero: neutral rooms
                    - Dies if health drops to 0 or below
                    
                    ## Algorithm
                    
                    Uses dynamic programming with bottom-up approach to efficiently calculate
                    the optimal path and minimum initial health requirement.
                    
                    ## Constraints
                    
                    - Grid dimensions: 1 ≤ m, n ≤ 200
                    - Cell values: -1000 ≤ value ≤ 100
                    - Minimum health: Always ≥ 1
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Dungeon Solver Team")
                    .email("support@dungeonsolverapi.com")
                    .url("https://github.com/dungeonsolverapi/dungeon-solver"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local development server"),
                new Server()
                    .url("https://api.dungeonsolverapi.com")
                    .description("Production server")
            ));
    }
}
