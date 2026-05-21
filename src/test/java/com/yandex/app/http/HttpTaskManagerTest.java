package com.yandex.app.http;

import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskManagerTest {
    private KVServer kvServer;

    @BeforeEach
    void setUp() throws Exception {
        kvServer = new KVServer();
        kvServer.start();
    }

    @AfterEach
    void tearDown() {
        kvServer.stop();
    }

    @Test
    void shouldPersistToKvAndLoadJson() {
        HttpTaskManager manager = new HttpTaskManager(new KVTaskClient("http://localhost:" + KVServer.PORT));
        manager.addTask(new Task(null, "t1", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,0), 15));

        List<Task> loaded = manager.load();
        assertNotNull(loaded);
        assertFalse(loaded.isEmpty());
    }
}
