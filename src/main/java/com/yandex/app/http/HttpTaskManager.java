package com.yandex.app.http;

import com.yandex.app.exceptions.ManagerSaveException;
import com.yandex.app.service.FileBackedTasksManager;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(KVTaskClient kvTaskClient) {
        this.kvTaskClient = kvTaskClient;
    }

    @Override
    public void save() {
        try {
            kvTaskClient.put("tasks", "[]");
        } catch (RuntimeException e) {
            throw new ManagerSaveException("Unable to save tasks to KV storage", e);
        }
    }
}
