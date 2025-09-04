package com.dungeon.controller;

import com.dungeon.dto.DungeonRequest;
import com.dungeon.dto.DungeonResponse;
import com.dungeon.service.DungeonService;
import com.dungeon.service.DungeonSolvingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the DungeonController.
 */
@WebMvcTest(DungeonController.class)
@DisplayName("Dungeon Controller Integration Tests")
class DungeonControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DungeonService dungeonService;
    
    @Test
    @DisplayName("Should return successful response for valid dungeon")
    void shouldReturnSuccessfulResponseForValidDungeon() throws Exception {
        // Given
        int[][] input = {{-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}};
        int[][] path = {{0, 0}, {0, 1}, {0, 2}, {1, 2}, {2, 2}};
        
        DungeonRequest request = new DungeonRequest(input);
        DungeonResponse expectedResponse = new DungeonResponse(input, path, 7);
        
        when(dungeonService.solveDungeon(any(DungeonRequest.class)))
            .thenReturn(expectedResponse);
        
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.min_hp").value(7))
                .andExpect(jsonPath("$.path").isArray())
                .andExpect(jsonPath("$.path.length()").value(5))
                .andExpect(jsonPath("$.input").isArray());
    }
    
    @Test
    @DisplayName("Should return 400 for null input")
    void shouldReturn400ForNullInput() throws Exception {
        // Given
        String invalidJson = "{\"input\": null}";
        
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }
    
    @Test
    @DisplayName("Should return 422 for unsolvable dungeon")
    void shouldReturn422ForUnsolvableDungeon() throws Exception {
        // Given
        int[][] input = {{-1000, -1000}, {-1000, -1000}};
        DungeonRequest request = new DungeonRequest(input);
        
        when(dungeonService.solveDungeon(any(DungeonRequest.class)))
            .thenThrow(new DungeonSolvingException("Dungeon is unsolvable", "UNSOLVABLE"));
        
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errorCode").value("UNSOLVABLE"));
    }
    
    @Test
    @DisplayName("Should return 500 for internal server error")
    void shouldReturn500ForInternalServerError() throws Exception {
        // Given
        int[][] input = {{1, 2}, {3, 4}};
        DungeonRequest request = new DungeonRequest(input);
        
        when(dungeonService.solveDungeon(any(DungeonRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errorCode").value("PROCESSING_ERROR"));
    }
    
    @Test
    @DisplayName("Should return health check response")
    void shouldReturnHealthCheckResponse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dungeon/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Dungeon Solver API"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    @DisplayName("Should handle empty JSON request")
    void shouldHandleEmptyJsonRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/dungeon/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}
