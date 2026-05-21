package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.FileBackedTasksManager;
import com.yandex.app.service.TaskManager;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class App {
    public static void main(String[] args) {
        Path storage = Path.of("kanban.csv");
        TaskManager manager = new FileBackedTasksManager(storage);

        Task task = new Task(null, "Task #1", "Standalone task", TaskStatus.NEW,
                LocalDateTime.of(2026, 5, 21, 10, 0), 60);
        manager.addTask(task);

        Epic epic = new Epic(null, "Epic #1", "Big feature", TaskStatus.NEW,
                null, 0, null, null);
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask(null, "Subtask #1", "First part", TaskStatus.NEW,
                LocalDateTime.of(2026, 5, 21, 12, 0), 30, epic.getId());
        Subtask subtask2 = new Subtask(null, "Subtask #2", "Second part", TaskStatus.DONE,
                LocalDateTime.of(2026, 5, 21, 13, 0), 90, epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        System.out.println("=== Before reload ===");
        printState(manager);

        FileBackedTasksManager reloaded = FileBackedTasksManager.loadFromFile(storage);
        System.out.println("=== After reload ===");
        printState(reloaded);
    }

    private static void printState(TaskManager manager) {
        System.out.println("Tasks: " + manager.getListOfTasks());
        System.out.println("Epics: " + manager.getListOfEpics());
        System.out.println("Subtasks: " + manager.getListOfSubtasks());
        System.out.println("Prioritized: " + manager.getPrioritizedTasks());
    }
}
