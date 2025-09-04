package com.dungeon.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a cell in a dungeon.
 */
@Entity
@Table(name = "cells", indexes = {
    @Index(name = "idx_cells_dungeon_id", columnList = "dungeon_id"),
    @Index(name = "idx_cells_position", columnList = "dungeon_id, row_index, col_index", unique = true)
})
public class Cell {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "cell_id", updatable = false, nullable = false)
    private UUID cellId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dungeon_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cells_dungeon"))
    private Dungeon dungeon;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    @Column(name = "col_index", nullable = false)
    private Integer colIndex;

    @Column(name = "value", nullable = false)
    private Integer value;

    @OneToMany(mappedBy = "cell", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SolutionPath> solutionPaths;

    // Constructors
    public Cell() {}

    public Cell(Dungeon dungeon, Integer rowIndex, Integer colIndex, Integer value) {
        this.dungeon = dungeon;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.value = value;
    }

    // Getters and Setters
    public UUID getCellId() {
        return cellId;
    }

    public void setCellId(UUID cellId) {
        this.cellId = cellId;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Integer getColIndex() {
        return colIndex;
    }

    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<SolutionPath> getSolutionPaths() {
        return solutionPaths;
    }

    public void setSolutionPaths(List<SolutionPath> solutionPaths) {
        this.solutionPaths = solutionPaths;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "cellId=" + cellId +
                ", rowIndex=" + rowIndex +
                ", colIndex=" + colIndex +
                ", value=" + value +
                '}';
    }
}
