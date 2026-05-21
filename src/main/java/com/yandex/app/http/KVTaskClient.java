package com.yandex.app.http;

import com.yandex.app.exceptions.RequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String apiToken;

    public KVTaskClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.apiToken = register();
    }

    public void put(String key, String value) {
        if (key == null || key.isBlank()) throw new RequestException("Key must not be blank");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/save/" + key + "?API_TOKEN=" + apiToken))
                .POST(HttpRequest.BodyPublishers.ofString(value == null ? "" : value))
                .build();
        send(request);
    }

    public String load(String key) {
        if (key == null || key.isBlank()) throw new RequestException("Key must not be blank");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/load/" + key + "?API_TOKEN=" + apiToken))
                .GET()
                .build();
        return send(request);
    }

    private String register() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/register")).GET().build();
        return send(request);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) throw new RequestException("Request failed: " + response.statusCode());
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestException("Request failed", e);
        }
    }
}
