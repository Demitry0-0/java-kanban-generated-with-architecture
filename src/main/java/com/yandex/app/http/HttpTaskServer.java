package com.yandex.app.http;

import com.yandex.app.service.TaskManager;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private final TasksHandler tasksHandler;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.tasksHandler = new TasksHandler();
    }

    public void start() {}
    public void stop() {}

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public TasksHandler getTasksHandler() {
        return tasksHandler;
    }
}
