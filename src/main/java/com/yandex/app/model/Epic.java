package com.yandex.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtasksId = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic() {
        super();
    }

    public Epic(Integer id, String name, String description, TaskStatus status, LocalDateTime startTime,
                long duration, List<Integer> subtasksId, LocalDateTime endTime) {
        super(id, name, description, status, startTime, duration);
        this.subtasksId = subtasksId == null ? new ArrayList<>() : new ArrayList<>(subtasksId);
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtasksId() {
        return new ArrayList<>(subtasksId);
    }

    public void setSubtasksId(List<Integer> subtasksId) {
        this.subtasksId = subtasksId == null ? new ArrayList<>() : new ArrayList<>(subtasksId);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Epic epic = (Epic) o;
        return Objects.equals(subtasksId, epic.subtasksId) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasksId, endTime);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "base=" + super.toString() +
                ", subtasksId=" + subtasksId +
                ", endTime=" + endTime +
                '}';
    }
}
