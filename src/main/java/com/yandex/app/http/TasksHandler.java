package com.yandex.app.http;

public class TasksHandler {
    public Endpoint resolveEndpoint(String path) {
        if (path == null || path.isBlank()) {
            return Endpoint.UNKNOWN;
        }
        if (path.startsWith("/tasks")) {
            return Endpoint.TASKS;
        }
        return Endpoint.UNKNOWN;
    }
}
