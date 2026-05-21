package com.yandex.app;

import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;

public class App {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        System.out.println("Kanban app initialized: " + taskManager.getClass().getSimpleName());
    }
}
