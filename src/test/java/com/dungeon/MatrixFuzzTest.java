package com.dungeon;

import com.dungeon.fuzz.DungeonMatrixGenerator;
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
import com.pholser.junit.quickcheck.From;
import org.junit.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(JQF.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MatrixFuzzTest {

    @Fuzz
    public void fuzzMatrixInput(@From(DungeonMatrixGenerator.class) String matrixJson) {
        String responseBody = null;
        int statusCode = -1;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";
            String jsonBody = "{\"input\": " + matrixJson + "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            responseBody = response.getBody();
            statusCode = response.getStatusCode().value();

            // Assert: No 5xx errors, response body is not null
            Assert.assertNotNull("Response body should not be null", responseBody);
            Assert.assertTrue("Should not return 5xx error", statusCode < 500);
        } catch (Exception e) {
            responseBody = e.getMessage();
            statusCode = 500;
            Assert.assertFalse("Should not throw server error", e.getMessage().contains("500"));
        } finally {
            // Log input and output to a file
            logFuzzCase(matrixJson, responseBody, statusCode);
        }
    }

    private void logFuzzCase(String input, String output, int statusCode) {
        String logDir = "logs/fuzz-cases";
        try {
            Files.createDirectories(Paths.get(logDir));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String uuid = UUID.randomUUID().toString();
            String filename = String.format("%s/fuzzcase_%s_%s.log", logDir, timestamp, uuid);
            StringBuilder sb = new StringBuilder();
            sb.append("INPUT:\n").append(input).append("\n\n");
            sb.append("OUTPUT:\n").append(output).append("\n\n");
            sb.append("STATUS_CODE: ").append(statusCode).append("\n");
            Path filePath = Paths.get(filename).toAbsolutePath();
            Files.write(filePath, sb.toString().getBytes());
            System.out.println("[FUZZ LOG] Wrote fuzz case to: " + filePath);
        } catch (IOException ioe) {
            System.err.println("[FUZZ LOG] Failed to log fuzz case: " + ioe.getMessage());
        }
    }
}
