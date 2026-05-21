package com.yandex.app.http;

public class TasksHandler {
    public Endpoint resolveEndpoint(String path) {
        if (path == null || path.isBlank()) {
            return Endpoint.UNKNOWN;
        }
        String normalized = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;

        if ("/tasks/task".equals(normalized)) return Endpoint.TASKS;
        if ("/tasks/task/".equals(path)) return Endpoint.TASKS;
        if ("/tasks/epic".equals(normalized)) return Endpoint.EPICS;
        if ("/tasks/subtask".equals(normalized)) return Endpoint.SUBTASKS;
        if ("/tasks/history".equals(normalized)) return Endpoint.HISTORY;
        if ("/tasks".equals(normalized)) return Endpoint.PRIORITIZED;

        if (normalized.startsWith("/tasks/task/")) return Endpoint.TASK;
        if (normalized.startsWith("/tasks/epic/subtask/")) return Endpoint.EPIC_SUBTASKS;
        if (normalized.startsWith("/tasks/epic/")) return Endpoint.EPIC;
        if (normalized.startsWith("/tasks/subtask/")) return Endpoint.SUBTASK;

        return Endpoint.UNKNOWN;
    }
}
