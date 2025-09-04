package com.dungeon.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entity representing a cell in a solution path for a dungeon.
 */
@Entity
@Table(name = "solution_paths", indexes = {
    @Index(name = "idx_solution_paths_dungeon", columnList = "dungeon_id, position")
})
@IdClass(SolutionPathId.class)
public class SolutionPath {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dungeon_id", nullable = false, foreignKey = @ForeignKey(name = "fk_solution_paths_dungeon"))
    private Dungeon dungeon;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", nullable = false, foreignKey = @ForeignKey(name = "fk_solution_paths_cell"))
    private Cell cell;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "min_hp", nullable = false)
    private Integer minHp;

    // Constructors
    public SolutionPath() {}

    public SolutionPath(Dungeon dungeon, Cell cell, Integer position, Integer minHp) {
        this.dungeon = dungeon;
        this.cell = cell;
        this.position = position;
        this.minHp = minHp;
    }

    // Getters and Setters
    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getMinHp() {
        return minHp;
    }

    public void setMinHp(Integer minHp) {
        this.minHp = minHp;
    }

    @Override
    public String toString() {
        return "SolutionPath{" +
                "position=" + position +
                ", minHp=" + minHp +
                ", cellRowIndex=" + (cell != null ? cell.getRowIndex() : null) +
                ", cellColIndex=" + (cell != null ? cell.getColIndex() : null) +
                '}';
    }
}
