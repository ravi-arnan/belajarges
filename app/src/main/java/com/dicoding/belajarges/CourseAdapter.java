package com.dicoding.belajarges;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final Context context;
    private List<Course> courseList;
    private final CourseListener listener;

    public interface CourseListener {
        void onAddTask(Course course);
        void onEditCourse(Course course);
        void onDeleteCourse(Course course);
        // Teruskan event dari TaskAdapter ke MainActivity
        void onTaskCheckChanged(Task task, boolean isChecked);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    public CourseAdapter(Context context, List<Course> courseList, CourseListener listener) {
        this.context = context;
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.tvCourseName.setText(course.getName());

        holder.btnAddTask.setOnClickListener(v -> listener.onAddTask(course));
        holder.btnEditCourse.setOnClickListener(v -> listener.onEditCourse(course));
        holder.btnDeleteCourse.setOnClickListener(v -> listener.onDeleteCourse(course));

        // Setup nested RecyclerView untuk tugas
        holder.rvTasks.setLayoutManager(new LinearLayoutManager(context));
        TaskAdapter taskAdapter = new TaskAdapter(context, course.getTasks(), new TaskAdapter.TaskListener() {
            @Override
            public void onTaskCheckChanged(Task task, boolean isChecked) {
                listener.onTaskCheckChanged(task, isChecked);
            }

            @Override
            public void onEditTask(Task task) {
                listener.onEditTask(task);
            }

            @Override
            public void onDeleteTask(Task task) {
                listener.onDeleteTask(task);
            }
        });
        holder.rvTasks.setAdapter(taskAdapter);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName;
        ImageButton btnAddTask, btnEditCourse, btnDeleteCourse;
        RecyclerView rvTasks;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            btnAddTask = itemView.findViewById(R.id.btn_add_task);
            btnEditCourse = itemView.findViewById(R.id.btn_edit_course);
            btnDeleteCourse = itemView.findViewById(R.id.btn_delete_course);
            rvTasks = itemView.findViewById(R.id.rv_tasks);
        }
    }
}
