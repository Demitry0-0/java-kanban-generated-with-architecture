package com.yandex.app.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yandex.app.exceptions.ManagerSaveException;
import com.yandex.app.model.LocalDateAdapter;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskManager extends InMemoryTaskManager {
    private static final String KEY = "tasks";
    private final KVTaskClient kvTaskClient;
    private final Gson gson;

    public HttpTaskManager(KVTaskClient kvTaskClient) {
        this.kvTaskClient = kvTaskClient;
        LocalDateAdapter adapter = new LocalDateAdapter();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> src == null ? null : new com.google.gson.JsonPrimitive(adapter.serialize(src)))
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>)
                        (json, typeOfT, context) -> json == null ? null : adapter.deserialize(json.getAsString()))
                .create();
    }

    @Override public void addTask(Task task) { mutateAndSave(() -> super.addTask(task)); }
    @Override public void addEpic(com.yandex.app.model.Epic epic) { mutateAndSave(() -> super.addEpic(epic)); }
    @Override public void addSubtask(com.yandex.app.model.Subtask subtask) { mutateAndSave(() -> super.addSubtask(subtask)); }
    @Override public void updateTask(Task task) { mutateAndSave(() -> super.updateTask(task)); }
    @Override public void updateEpic(com.yandex.app.model.Epic epic) { mutateAndSave(() -> super.updateEpic(epic)); }
    @Override public void updateSubtask(com.yandex.app.model.Subtask subtask) { mutateAndSave(() -> super.updateSubtask(subtask)); }
    @Override public void removeTaskById(int id) { mutateAndSave(() -> super.removeTaskById(id)); }
    @Override public void removeEpicById(int id) { mutateAndSave(() -> super.removeEpicById(id)); }
    @Override public void removeSubtaskById(int id) { mutateAndSave(() -> super.removeSubtaskById(id)); }
    @Override public void removeAllTasks() { mutateAndSave(super::removeAllTasks); }
    @Override public void removeAllEpics() { mutateAndSave(super::removeAllEpics); }
    @Override public void removeAllSubtasks() { mutateAndSave(super::removeAllSubtasks); }

    private void mutateAndSave(Runnable mutation) {
        mutation.run();
        save();
    }

    public void save() {
        try {
            String payload = gson.toJson(getPrioritizedTasks());
            kvTaskClient.put(KEY, payload);
        } catch (RuntimeException e) {
            throw new ManagerSaveException("Unable to save to KV", e);
        }
    }

    public List<Task> load() {
        try {
            Type type = new TypeToken<List<Task>>() {}.getType();
            return gson.fromJson(kvTaskClient.load(KEY), type);
        } catch (RuntimeException e) {
            throw new ManagerSaveException("Unable to load from KV", e);
        }
    }
}
