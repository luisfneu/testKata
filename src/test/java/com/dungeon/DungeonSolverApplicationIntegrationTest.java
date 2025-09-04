package com.dungeon;

import com.dungeon.dto.DungeonRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for the entire Dungeon Solver application.
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Dungeon Solver Application Integration Tests")
class DungeonSolverApplicationIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Should solve the example dungeon end-to-end")
    void shouldSolveExampleDungeonEndToEnd() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Given - the example dungeon from the problem description
        int[][] input = {{-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}};
        DungeonRequest request = new DungeonRequest(input);
        
        // When & Then - solve the dungeon
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.min_hp").value(7))
                .andExpect(jsonPath("$.path").isArray())
                .andExpect(jsonPath("$.path.length()").value(5))
                .andExpect(jsonPath("$.path[0][0]").value(0))
                .andExpect(jsonPath("$.path[0][1]").value(0))
                .andExpect(jsonPath("$.path[4][0]").value(2))
                .andExpect(jsonPath("$.path[4][1]").value(2))
                .andExpect(jsonPath("$.input").isArray());
    }
    
    @Test
    @DisplayName("Should provide health check endpoint")
    void shouldProvideHealthCheckEndpoint() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/dungeon/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Dungeon Solver API"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    @DisplayName("Should handle validation errors properly")
    void shouldHandleValidationErrorsProperly() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Test with empty dungeon
        DungeonRequest invalidRequest = new DungeonRequest(new int[0][]);
        
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/dungeon/solve"));
    }
}
