package com.yandex.app.service;

import com.yandex.app.exceptions.CollisionTaskException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager;

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this(new InMemoryHistoryManager());
    }

    @Override
    public void addTask(Task task) {
        validate(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null || subtask.getEpicId() == null) {
            throw new IllegalArgumentException("Subtask and epicId must not be null");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic with id=" + subtask.getEpicId() + " does not exist");
        }

        validate(subtask);
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        List<Integer> ids = epic.getSubtasksId();
        ids.add(subtask.getId());
        epic.setSubtasksId(ids);

        refreshEpic(subtask.getEpicId());
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic must not be null");
        }
        epic.validate();
        epic.setId(generateId());
        epic.setSubtasksId(new ArrayList<>());
        epic.setStatus(TaskStatus.NEW);
        epic.setDuration(0);
        epic.setStartTime(null);
        epic.setEndTime(null);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void removeAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.setSubtasksId(new ArrayList<>());
            refreshEpic(epic.getId());
        }
    }

    @Override
    public void removeAllEpics() {
        removeAllSubtasks();
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
    }

    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getListOfSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> getListOfSubtasksByOneEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtasksId().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }

        for (Integer subtaskId : epic.getSubtasksId()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            List<Integer> ids = epic.getSubtasksId();
            ids.remove(Integer.valueOf(id));
            epic.setSubtasksId(ids);
            refreshEpic(epic.getId());
        }

        historyManager.remove(id);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || task.getId() == null || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task with id must exist for update");
        }
        validate(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || epic.getId() == null || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Epic with id must exist for update");
        }

        Epic stored = epics.get(epic.getId());
        stored.setName(epic.getName());
        stored.setDescription(epic.getDescription());
        refreshEpic(stored.getId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || subtask.getId() == null || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Subtask with id must exist for update");
        }

        Subtask old = subtasks.get(subtask.getId());
        if (!Objects.equals(old.getEpicId(), subtask.getEpicId())) {
            throw new IllegalArgumentException("Changing epicId of existing subtask is not allowed");
        }

        validate(subtask);
        subtasks.put(subtask.getId(), subtask);
        refreshEpic(subtask.getEpicId());
    }

    @Override
    public void checkEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getListOfSubtasksByOneEpic(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(sub -> sub.getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(sub -> sub.getStatus() == TaskStatus.DONE);

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void setEpicDateTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getListOfSubtasksByOneEpic(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
            return;
        }

        LocalDateTime start = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long duration = epicSubtasks.stream().mapToLong(Subtask::getDuration).sum();

        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(duration);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> all = new ArrayList<>();
        all.addAll(tasks.values());
        all.addAll(subtasks.values());

        all.sort(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(Task::getId, Comparator.nullsLast(Integer::compareTo)));
        return all;
    }

    @Override
    public void validate(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        task.validate();
        validateNoTimeIntersection(task);
    }

    protected int generateId() {
        return nextId++;
    }

    private void refreshEpic(int epicId) {
        checkEpicStatus(epicId);
        setEpicDateTime(epicId);
    }

    private void validateNoTimeIntersection(Task candidate) {
        if (candidate.getStartTime() == null || candidate.getEndTime() == null) {
            return;
        }

        for (Task existing : collectScheduledTasks()) {
            if (existing.getId() != null && existing.getId().equals(candidate.getId())) {
                continue;
            }
            if (existing.getStartTime() == null || existing.getEndTime() == null) {
                continue;
            }

            boolean intersects = candidate.getStartTime().isBefore(existing.getEndTime())
                    && existing.getStartTime().isBefore(candidate.getEndTime());
            if (intersects) {
                throw new CollisionTaskException("Task time intersects with task id=" + existing.getId());
            }
        }
    }

    private Collection<Task> collectScheduledTasks() {
        List<Task> result = new ArrayList<>(tasks.values());
        result.addAll(subtasks.values());
        return result;
    }
}
