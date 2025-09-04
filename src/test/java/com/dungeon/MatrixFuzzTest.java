package com.dungeon;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(JQF.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MatrixFuzzTest {

    @Fuzz
    public void fuzzMatrixInput(String input) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";
            String jsonBody = "{\"input\": " + input + "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());

        }
    }
}

