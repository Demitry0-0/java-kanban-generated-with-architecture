package com.yandex.app.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.yandex.app.model.Epic;
import com.yandex.app.model.LocalDateAdapter;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private final TasksHandler router;
    private final HttpServer server;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.router = new TasksHandler();
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        LocalDateAdapter adapter = new LocalDateAdapter();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                        (src, t, c) -> src == null ? null : new com.google.gson.JsonPrimitive(adapter.serialize(src)))
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>)
                        (json, t, c) -> json == null ? null : adapter.deserialize(json.getAsString()))
                .create();
        server.createContext("/tasks", this::handle);
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = router.resolveEndpoint(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod();

            switch (endpoint) {
                case TASKS: handleTasks(exchange, method); break;
                case TASK: handleTaskById(exchange, method); break;
                case EPICS: handleEpics(exchange, method); break;
                case EPIC: handleEpicById(exchange, method); break;
                case SUBTASKS: handleSubtasks(exchange, method); break;
                case SUBTASK: handleSubtaskById(exchange, method); break;
                case EPIC_SUBTASKS: handleEpicSubtasks(exchange); break;
                case HISTORY: write(exchange, 200, gson.toJson(taskManager.getHistory())); break;
                case PRIORITIZED: write(exchange, 200, gson.toJson(taskManager.getPrioritizedTasks())); break;
                default: write(exchange, 404, "Not found");
            }
        } catch (IllegalArgumentException e) {
            write(exchange, 400, e.getMessage());
        } catch (Exception e) {
            write(exchange, 500, e.getMessage());
        }
    }

    private void handleTasks(HttpExchange ex, String method) throws IOException {
        if ("GET".equals(method)) write(ex, 200, gson.toJson(taskManager.getListOfTasks()));
        else if ("POST".equals(method)) { taskManager.addTask(gson.fromJson(read(ex), Task.class)); write(ex, 201, "created"); }
        else if ("DELETE".equals(method)) { taskManager.removeAllTasks(); write(ex, 200, "deleted"); }
        else write(ex, 400, "Unsupported method");
    }

    private void handleTaskById(HttpExchange ex, String method) throws IOException {
        int id = extractId(ex.getRequestURI().getPath());
        if ("GET".equals(method)) { Task t = taskManager.getTaskById(id); if (t==null) write(ex,404,"Not found"); else write(ex,200,gson.toJson(t)); }
        else if ("POST".equals(method)) { Task t = gson.fromJson(read(ex), Task.class); t.setId(id); taskManager.updateTask(t); write(ex,200,"updated"); }
        else if ("DELETE".equals(method)) { taskManager.removeTaskById(id); write(ex,200,"deleted"); }
        else write(ex,400,"Unsupported method");
    }

    private void handleEpics(HttpExchange ex, String method) throws IOException {
        if ("GET".equals(method)) write(ex, 200, gson.toJson(taskManager.getListOfEpics()));
        else if ("POST".equals(method)) { taskManager.addEpic(gson.fromJson(read(ex), Epic.class)); write(ex, 201, "created"); }
        else if ("DELETE".equals(method)) { taskManager.removeAllEpics(); write(ex, 200, "deleted"); }
        else write(ex, 400, "Unsupported method");
    }

    private void handleEpicById(HttpExchange ex, String method) throws IOException {
        int id = extractId(ex.getRequestURI().getPath());
        if ("GET".equals(method)) { Epic e = taskManager.getEpicById(id); if (e==null) write(ex,404,"Not found"); else write(ex,200,gson.toJson(e)); }
        else if ("POST".equals(method)) { Epic e = gson.fromJson(read(ex), Epic.class); e.setId(id); taskManager.updateEpic(e); write(ex,200,"updated"); }
        else if ("DELETE".equals(method)) { taskManager.removeEpicById(id); write(ex,200,"deleted"); }
        else write(ex,400,"Unsupported method");
    }

    private void handleSubtasks(HttpExchange ex, String method) throws IOException {
        if ("GET".equals(method)) write(ex, 200, gson.toJson(taskManager.getListOfSubtasks()));
        else if ("POST".equals(method)) { taskManager.addSubtask(gson.fromJson(read(ex), Subtask.class)); write(ex, 201, "created"); }
        else if ("DELETE".equals(method)) { taskManager.removeAllSubtasks(); write(ex, 200, "deleted"); }
        else write(ex, 400, "Unsupported method");
    }

    private void handleSubtaskById(HttpExchange ex, String method) throws IOException {
        int id = extractId(ex.getRequestURI().getPath());
        if ("GET".equals(method)) { Subtask s = taskManager.getSubtaskById(id); if (s==null) write(ex,404,"Not found"); else write(ex,200,gson.toJson(s)); }
        else if ("POST".equals(method)) { Subtask s = gson.fromJson(read(ex), Subtask.class); s.setId(id); taskManager.updateSubtask(s); write(ex,200,"updated"); }
        else if ("DELETE".equals(method)) { taskManager.removeSubtaskById(id); write(ex,200,"deleted"); }
        else write(ex,400,"Unsupported method");
    }

    private void handleEpicSubtasks(HttpExchange ex) throws IOException {
        int id = extractId(ex.getRequestURI().getPath());
        List<Subtask> subtasks = taskManager.getListOfSubtasksByOneEpic(id);
        write(ex, 200, gson.toJson(subtasks));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private String read(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void write(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
