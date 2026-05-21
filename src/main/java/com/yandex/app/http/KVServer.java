package com.yandex.app.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KVServer {
    public static final int PORT = 8078;

    private final HttpServer server;
    private final Gson gson = new Gson();
    private final Map<String, String> data = new HashMap<>();
    private final String apiToken = UUID.randomUUID().toString();

    public KVServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.server.createContext("/register", new RegisterHandler());
        this.server.createContext("/save", new SaveHandler());
        this.server.createContext("/load", new LoadHandler());
    }

    public String getApiToken() {
        return apiToken;
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    private boolean isAuthorized(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        return query != null && query.equals("API_TOKEN=" + apiToken);
    }

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            write(exchange, 200, apiToken);
        }
    }

    private class SaveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                write(exchange, 405, "Only POST allowed");
                return;
            }
            if (!isAuthorized(exchange)) {
                write(exchange, 403, "Forbidden");
                return;
            }
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3 || parts[2].isBlank()) {
                write(exchange, 400, "Key required");
                return;
            }
            String key = parts[2];
            String body = readBody(exchange);
            data.put(key, body);
            write(exchange, 200, gson.toJson(Map.of("status", "ok")));
        }
    }

    private class LoadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                write(exchange, 405, "Only GET allowed");
                return;
            }
            if (!isAuthorized(exchange)) {
                write(exchange, 403, "Forbidden");
                return;
            }
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3 || parts[2].isBlank()) {
                write(exchange, 400, "Key required");
                return;
            }
            String value = data.getOrDefault(parts[2], "");
            write(exchange, 200, value);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void write(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
