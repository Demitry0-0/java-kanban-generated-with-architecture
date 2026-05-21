package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void shouldSaveLoadAndRestoreEpicLinks() throws Exception {
        Path file = Files.createTempFile("kanban-test", ".csv");
        try {
            FileBackedTasksManager manager = new FileBackedTasksManager(file);
            Task task = new Task(null, "t", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,0), 20);
            Epic epic = new Epic(null, "e", "d", TaskStatus.NEW, null, 0, null, null);
            manager.addTask(task);
            manager.addEpic(epic);
            Subtask subtask = new Subtask(null, "s", "d", TaskStatus.DONE, LocalDateTime.of(2026,1,1,11,0), 30, epic.getId());
            manager.addSubtask(subtask);

            FileBackedTasksManager loaded = FileBackedTasksManager.loadFromFile(file);
            assertEquals(1, loaded.getListOfTasks().size());
            assertEquals(1, loaded.getListOfEpics().size());
            assertEquals(1, loaded.getListOfSubtasks().size());
            assertEquals(1, loaded.getEpicById(epic.getId()).getSubtasksId().size());
            assertEquals(TaskStatus.DONE, loaded.getEpicById(epic.getId()).getStatus());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
