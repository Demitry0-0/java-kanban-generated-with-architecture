package com.yandex.app.service;

import com.yandex.app.exceptions.CollisionTaskException;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldPerformCrudForTask() {
        Task task = new Task(null, "t1", "d1", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,0), 30);
        manager.addTask(task);
        assertNotNull(task.getId());
        assertEquals(1, manager.getListOfTasks().size());

        Task updated = new Task(task.getId(), "t1u", "d1u", TaskStatus.IN_PROGRESS, LocalDateTime.of(2026,1,1,11,0), 40);
        manager.updateTask(updated);
        assertEquals("t1u", manager.getTaskById(task.getId()).getName());

        manager.removeTaskById(task.getId());
        assertNull(manager.getTaskById(task.getId()));
    }

    @Test
    void shouldUpdateEpicStatusFromSubtasks() {
        Epic epic = new Epic(null, "e", "d", TaskStatus.NEW, null, 0, null, null);
        manager.addEpic(epic);
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());

        Subtask s1 = new Subtask(null, "s1", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,0), 10, epic.getId());
        Subtask s2 = new Subtask(null, "s2", "d", TaskStatus.DONE, LocalDateTime.of(2026,1,1,11,0), 10, epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());

        Subtask s1Done = new Subtask(s1.getId(), s1.getName(), s1.getDescription(), TaskStatus.DONE, s1.getStartTime(), s1.getDuration(), epic.getId());
        manager.updateSubtask(s1Done);
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldValidateTimeIntersections() {
        Task t1 = new Task(null, "t1", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,0), 60);
        manager.addTask(t1);

        Task crossing = new Task(null, "t2", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,10,30), 60);
        assertThrows(CollisionTaskException.class, () -> manager.addTask(crossing));
    }

    @Test
    void shouldReturnPrioritizedTasksByStartTime() {
        Task late = new Task(null, "late", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,12,0), 10);
        Task early = new Task(null, "early", "d", TaskStatus.NEW, LocalDateTime.of(2026,1,1,9,0), 10);
        manager.addTask(late);
        manager.addTask(early);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals("early", prioritized.get(0).getName());
        assertEquals("late", prioritized.get(1).getName());
    }
}
