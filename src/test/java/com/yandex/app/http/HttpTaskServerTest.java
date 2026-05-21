package com.yandex.app.http;

import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerTest {
    private HttpTaskServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws Exception {
        server = new HttpTaskServer(new InMemoryTaskManager());
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldSupportCrudAndSpecialEndpoints() throws IOException, InterruptedException {
        HttpResponse<String> createEpic = post("/tasks/epic", "{\"name\":\"Epic\",\"description\":\"D\",\"status\":\"NEW\",\"duration\":0}");
        assertEquals(201, createEpic.statusCode());

        HttpResponse<String> createTask = post("/tasks/task", "{\"name\":\"Task\",\"description\":\"D\",\"status\":\"NEW\",\"startTime\":\"2026-01-01T10:00:00\",\"duration\":10}");
        assertEquals(201, createTask.statusCode());

        HttpResponse<String> getTask = get("/tasks/task/2");
        assertEquals(200, getTask.statusCode());

        HttpResponse<String> notFound = get("/tasks/task/999");
        assertEquals(404, notFound.statusCode());

        HttpResponse<String> badId = get("/tasks/task/abc");
        assertEquals(400, badId.statusCode());

        assertEquals(200, get("/tasks/history").statusCode());
        assertEquals(200, get("/tasks").statusCode());
        assertEquals(200, get("/tasks/epic/subtask/1").statusCode());

        assertEquals(200, delete("/tasks/task/2").statusCode());
        assertEquals(200, delete("/tasks/task").statusCode());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
