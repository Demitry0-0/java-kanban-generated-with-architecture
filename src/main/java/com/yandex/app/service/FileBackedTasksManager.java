package com.yandex.app.service;

import com.yandex.app.exceptions.ManagerSaveException;

public class FileBackedTasksManager extends InMemoryTaskManager {
    public void save() {
        throw new ManagerSaveException("Save is not implemented yet");
    }
}
