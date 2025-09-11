package com.digitaltwin.trial.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "trial")
public class Trial implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "run_no")
    private String runNo;

    @Column(name = "mode")
    private String mode;

    @Column(name = "start_timestamp", nullable = false)
    private Long startTimestamp;

    @Column(name = "end_timestamp")
    private Long endTimestamp;

    // Constructors
    public Trial() {}

    public Trial(String name, Long startTimestamp) {
        this.name = name;
        this.startTimestamp = startTimestamp;
    }

    public Trial(String name, Long startTimestamp, String runNo, String mode) {
        this.name = name;
        this.startTimestamp = startTimestamp;
        this.runNo = runNo;
        this.mode = mode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRunNo() {
        return runNo;
    }

    public void setRunNo(String runNo) {
        this.runNo = runNo;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public String toString() {
        return "Trial{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", runNo='" + runNo + '\'' +
                ", mode='" + mode + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                '}';
    }
}