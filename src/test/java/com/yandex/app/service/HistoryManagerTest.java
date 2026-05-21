package com.yandex.app.service;

import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    private InMemoryHistoryManager history;

    @BeforeEach
    void setUp() {
        history = new InMemoryHistoryManager();
    }

    @Test
    void shouldKeepNoDuplicatesAndPreserveOrder() {
        Task t1 = new Task(1, "t1", "d", TaskStatus.NEW, LocalDateTime.now(), 10);
        Task t2 = new Task(2, "t2", "d", TaskStatus.NEW, LocalDateTime.now().plusHours(1), 10);

        history.add(t1);
        history.add(t2);
        history.add(t1);

        List<Task> result = history.getHistory();
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getId());
        assertEquals(1, result.get(1).getId());
    }

    @Test
    void shouldRemoveFromHistory() {
        Task t1 = new Task(1, "t1", "d", TaskStatus.NEW, LocalDateTime.now(), 10);
        Task t2 = new Task(2, "t2", "d", TaskStatus.NEW, LocalDateTime.now().plusHours(1), 10);
        history.add(t1);
        history.add(t2);

        history.remove(1);
        List<Task> result = history.getHistory();
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getId());
    }
}
