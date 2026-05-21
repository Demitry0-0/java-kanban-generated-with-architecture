package com.yandex.app.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask() {
        super();
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, LocalDateTime startTime,
                   long duration, Integer epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
        validate();
    }

    @Override
    public void validate() {
        super.validate();
        if (epicId != null && epicId < 0) {
            throw new IllegalArgumentException("Epic id must be non-negative");
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
        validate();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Subtask subtask = (Subtask) o;
        return Objects.equals(epicId, subtask.epicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "base=" + super.toString() +
                ", epicId=" + epicId +
                '}';
    }
}
