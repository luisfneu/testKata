package com.dungeon.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key class for SolutionPath entity.
 */
public class SolutionPathId implements Serializable {

    private UUID dungeon;
    private UUID cell;

    // Constructors
    public SolutionPathId() {}

    public SolutionPathId(UUID dungeon, UUID cell) {
        this.dungeon = dungeon;
        this.cell = cell;
    }

    // Getters and Setters
    public UUID getDungeon() {
        return dungeon;
    }

    public void setDungeon(UUID dungeon) {
        this.dungeon = dungeon;
    }

    public UUID getCell() {
        return cell;
    }

    public void setCell(UUID cell) {
        this.cell = cell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolutionPathId that = (SolutionPathId) o;
        return Objects.equals(dungeon, that.dungeon) && Objects.equals(cell, that.cell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dungeon, cell);
    }
}
