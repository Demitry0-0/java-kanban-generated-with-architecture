package com.yandex.app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String serialize(LocalDateTime value) {
        return value == null ? null : value.format(FORMATTER);
    }

    public LocalDateTime deserialize(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, FORMATTER);
    }
}
