package com.yandex.app.http;

import com.yandex.app.exceptions.RequestException;

public class KVTaskClient {
    public void put(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new RequestException("Key must not be blank");
        }
    }

    public String load(String key) {
        if (key == null || key.isBlank()) {
            throw new RequestException("Key must not be blank");
        }
        return "";
    }
}
