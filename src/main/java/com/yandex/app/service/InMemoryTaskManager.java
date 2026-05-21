package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() { this(new InMemoryHistoryManager()); }

    public void addTask(Task task) {}
    public void addSubtask(Subtask subtask) {}
    public void addEpic(Epic epic) {}
    public void removeAllTasks() {}
    public void removeAllSubtasks() {}
    public void removeAllEpics() {}
    public List<Task> getListOfTasks() { return new ArrayList<>(); }
    public List<Epic> getListOfEpics() { return new ArrayList<>(); }
    public List<Subtask> getListOfSubtasks() { return new ArrayList<>(); }
    public Task getTaskById(int id) { return null; }
    public Epic getEpicById(int id) { return null; }
    public Subtask getSubtaskById(int id) { return null; }
    public List<Subtask> getListOfSubtasksByOneEpic(int epicId) { return new ArrayList<>(); }
    public void removeTaskById(int id) {}
    public void removeEpicById(int id) {}
    public void removeSubtaskById(int id) {}
    public void updateTask(Task task) {}
    public void updateEpic(Epic epic) {}
    public void updateSubtask(Subtask subtask) {}
    public void checkEpicStatus(int epicId) {}
    public List<Task> getHistory() { return historyManager.getHistory(); }
    public void setEpicDateTime(int epicId) {}
    public List<Task> getPrioritizedTasks() { return new ArrayList<>(); }
    public void validate(Task task) { if (task == null) throw new IllegalArgumentException("Task is null"); task.validate(); }
}
