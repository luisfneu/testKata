package com.dungeon.dto;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for DungeonRequest to handle invalid input formats gracefully.
 * Provides better error messages when the input cannot be parsed as a 2D integer array.
 */
public class DungeonRequestDeserializer extends JsonDeserializer<DungeonRequest> {

    @Override
    public DungeonRequest deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JacksonException {

        JsonNode rootNode = parser.getCodec().readTree(parser);
        JsonNode inputNode = rootNode.get("input");

        if (inputNode == null) {
            throw new BadRequestDungeonInputException("Missing required field 'input'");
        }

        int[][] dungeonGrid;

        try {
            dungeonGrid = parseInputNode(inputNode);
        } catch (BadRequestDungeonInputException e) {
            throw e; // Re-throw our custom exception
        } catch (Exception e) {
            throw new BadRequestDungeonInputException(
                "Invalid dungeon input format. Expected a 2D array of integers, but received: " +
                inputNode.toString() + ". Please provide input in format: [[1,2,3],[4,5,6]]");
        }

        return new DungeonRequest(dungeonGrid);
    }

    private int[][] parseInputNode(JsonNode inputNode) throws BadRequestDungeonInputException {
        if (!inputNode.isArray()) {
            throw new BadRequestDungeonInputException(
                "Input must be an array, but received: " + inputNode.getNodeType());
        }

        List<int[]> rows = new ArrayList<>();

        for (JsonNode rowNode : inputNode) {
            if (!rowNode.isArray()) {
                throw new BadRequestDungeonInputException(
                    "Each row must be an array of integers, but received: " + rowNode.toString());
            }

            List<Integer> row = new ArrayList<>();
            for (JsonNode cellNode : rowNode) {
                if (!cellNode.isInt()) {
                    throw new BadRequestDungeonInputException(
                        "Each cell must be an integer, but received: " + cellNode.toString());
                }
                row.add(cellNode.asInt());
            }

            rows.add(row.stream().mapToInt(Integer::intValue).toArray());
        }

        return rows.toArray(new int[0][]);
    }
}
