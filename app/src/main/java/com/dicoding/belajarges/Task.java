package com.dicoding.belajarges;

public class Task {
    private String id;
    private String courseId;
    private String title;
    private long deadline; // Simpan sebagai timestamp (milliseconds)
    private boolean completed;

    // Diperlukan constructor kosong untuk Firebase
    public Task() {}

    public Task(String id, String courseId, String title, long deadline, boolean completed) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.deadline = deadline;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
