package com.dungeon.fuzz;

import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.*;

public class DungeonMatrixGenerator extends Generator<String> {
    public DungeonMatrixGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        int rows = random.nextInt(1, 5);
        int cols = random.nextInt(1, 5);
        List<List<Object>> matrix = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Object> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(randomCell(random, 0));
            }
            matrix.add(row);
        }
        return toJson(matrix);
    }

    private Object randomCell(SourceOfRandomness random, int depth) {
        int type = random.nextInt(0, depth < 2 ? 2 : 1); // 0: int, 1: string, 2: nested array
        switch (type) {
            case 0: return random.nextInt(-20, 20);
            case 1: return random.nextBoolean() ? random.nextInt(-20, 20) : randomString(random);
            case 2: // nested array
                int size = random.nextInt(1, 3);
                List<Object> nested = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    nested.add(randomCell(random, depth + 1));
                }
                return nested;
            default: return 0;
        }
    }

    private String randomString(SourceOfRandomness random) {
        int len = random.nextInt(1, 5);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) random.nextInt(97, 122)); // a-z
        }
        return sb.toString();
    }

    // Simple JSON serialization for matrix
    private String toJson(Object obj) {
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        } else if (obj instanceof String) {
            return "\"" + obj + "\"";
        } else {
            return obj.toString();
        }
    }
}

