package com.yandex.app.service;

import com.yandex.app.exceptions.ManagerSaveException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.model.TaskType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private static final String HEADER = "id,type,name,status,description,startTime,duration,epic";
    private final Path file;

    public FileBackedTasksManager(Path file) {
        super();
        this.file = file;
    }

    public static FileBackedTasksManager loadFromFile(Path file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        if (!Files.exists(file)) {
            return manager;
        }

        int maxId = 0;
        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.isBlank()) {
                    continue;
                }
                Task task = fromString(line);
                manager.restore(task);
                if (task.getId() != null && task.getId() > maxId) {
                    maxId = task.getId();
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    List<Integer> ids = epic.getSubtasksId();
                    ids.add(subtask.getId());
                    epic.setSubtasksId(ids);
                }
            }
            for (Epic epic : manager.epics.values()) {
                manager.checkEpicStatus(epic.getId());
                manager.setEpicDateTime(epic.getId());
            }
            manager.setNextId(maxId + 1);
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load tasks from file: " + file, e);
        }

        return manager;
    }

    @Override
    public void addTask(Task task) {
        mutateAndSave(() -> super.addTask(task));
    }

    @Override
    public void addSubtask(Subtask subtask) {
        mutateAndSave(() -> super.addSubtask(subtask));
    }

    @Override
    public void addEpic(Epic epic) {
        mutateAndSave(() -> super.addEpic(epic));
    }

    @Override
    public void removeAllTasks() {
        mutateAndSave(super::removeAllTasks);
    }

    @Override
    public void removeAllSubtasks() {
        mutateAndSave(super::removeAllSubtasks);
    }

    @Override
    public void removeAllEpics() {
        mutateAndSave(super::removeAllEpics);
    }

    @Override
    public void removeTaskById(int id) {
        mutateAndSave(() -> super.removeTaskById(id));
    }

    @Override
    public void removeEpicById(int id) {
        mutateAndSave(() -> super.removeEpicById(id));
    }

    @Override
    public void removeSubtaskById(int id) {
        mutateAndSave(() -> super.removeSubtaskById(id));
    }

    @Override
    public void updateTask(Task task) {
        mutateAndSave(() -> super.updateTask(task));
    }

    @Override
    public void updateEpic(Epic epic) {
        mutateAndSave(() -> super.updateEpic(epic));
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        mutateAndSave(() -> super.updateSubtask(subtask));
    }


    private void mutateAndSave(Runnable mutation) {
        mutation.run();
        save();
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(HEADER);
            writer.newLine();

            for (Task task : tasks.values()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save tasks to file: " + file, e);
        }
    }

    private void restore(Task task) {
        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            subtasks.put(task.getId(), (Subtask) task);
        } else {
            tasks.put(task.getId(), task);
        }
    }

    private static String toString(Task task) {
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                task.getStartTime() == null ? "" : task.getStartTime().toString(),
                String.valueOf(task.getDuration()),
                epicId
        );
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        LocalDateTime startTime = fields[5].isBlank() ? null : LocalDateTime.parse(fields[5]);
        long duration = Long.parseLong(fields[6]);

        if (type == TaskType.EPIC) {
            return new Epic(id, name, description, status, startTime, duration, new ArrayList<>(), null);
        }
        if (type == TaskType.SUBTASK) {
            Integer epicId = fields[7].isBlank() ? null : Integer.parseInt(fields[7]);
            return new Subtask(id, name, description, status, startTime, duration, epicId);
        }
        return new Task(id, name, description, status, startTime, duration);
    }


}
