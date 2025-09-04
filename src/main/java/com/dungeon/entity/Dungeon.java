package com.dungeon.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a dungeon in the database.
 */
@Entity
@Table(name = "dungeon", indexes = {
    @Index(name = "idx_dungeon_hash_input", columnList = "hash_input", unique = true)
})
public class Dungeon {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "hash_input", nullable = false, unique = true, length = 64)
    private String hashInput;

    @Column(name = "rows", nullable = false)
    private Integer rows;

    @Column(name = "cols", nullable = false)
    private Integer cols;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dungeon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cell> cells;

    @OneToMany(mappedBy = "dungeon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SolutionPath> solutionPaths;

    // Constructors
    public Dungeon() {
        this.createdAt = LocalDateTime.now();
    }

    public Dungeon(String hashInput, Integer rows, Integer cols) {
        this();
        this.hashInput = hashInput;
        this.rows = rows;
        this.cols = cols;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHashInput() {
        return hashInput;
    }

    public void setHashInput(String hashInput) {
        this.hashInput = hashInput;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer cols) {
        this.cols = cols;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    public List<SolutionPath> getSolutionPaths() {
        return solutionPaths;
    }

    public void setSolutionPaths(List<SolutionPath> solutionPaths) {
        this.solutionPaths = solutionPaths;
    }

    @Override
    public String toString() {
        return "Dungeon{" +
                "id=" + id +
                ", hashInput='" + hashInput + '\'' +
                ", rows=" + rows +
                ", cols=" + cols +
                ", createdAt=" + createdAt +
                '}';
    }
}
