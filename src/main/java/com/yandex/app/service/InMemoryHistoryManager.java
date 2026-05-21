package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) { if (task != null) history.add(task); }

    @Override
    public void remove(int id) { history.removeIf(task -> task.getId() != null && task.getId() == id); }

    @Override
    public List<Task> getHistory() { return new ArrayList<>(history); }
}
