package com.dicoding.belajarges;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String id;
    private String name;
    // List tugas tidak disimpan di Firestore untuk menghindari data duplikat,
    // tapi digunakan di adapter untuk kemudahan.
    private transient List<Task> tasks;

    // Diperlukan constructor kosong untuk Firebase
    public Course() {
        this.tasks = new ArrayList<>();
    }

    public Course(String id, String name) {
        this.id = id;
        this.name = name;
        this.tasks = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
