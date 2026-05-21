package com.yandex.app.http;

import com.yandex.app.exceptions.ManagerSaveException;
import com.yandex.app.service.FileBackedTasksManager;

import java.nio.file.Path;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(KVTaskClient kvTaskClient, Path file) {
        super(file);
        this.kvTaskClient = kvTaskClient;
    }

    @Override
    public void save() {
        super.save();
        try {
            kvTaskClient.put("tasks", "saved");
        } catch (RuntimeException e) {
            throw new ManagerSaveException("Unable to save tasks to KV storage", e);
        }
    }
}
