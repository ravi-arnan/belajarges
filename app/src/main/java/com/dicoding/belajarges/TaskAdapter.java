package com.dicoding.belajarges;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final Context context;
    private final TaskListener listener;

    public interface TaskListener {
        void onTaskCheckChanged(Task task, boolean isChecked);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, TaskListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTaskTitle.setText(task.getTitle());

        // Format deadline
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvTaskDeadline.setText(sdf.format(new Date(task.getDeadline())));

        // Atur status checkbox tanpa memicu listener
        holder.cbTaskCompleted.setOnCheckedChangeListener(null);
        holder.cbTaskCompleted.setChecked(task.isCompleted());
        updateTaskAppearance(holder, task.isCompleted());

        // Atur kembali listener setelah mengatur status
        holder.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTaskAppearance(holder, isChecked);
            listener.onTaskCheckChanged(task, isChecked);
        });

        holder.btnEditTask.setOnClickListener(v -> listener.onEditTask(task));
        holder.btnDeleteTask.setOnClickListener(v -> listener.onDeleteTask(task));
    }

    private void updateTaskAppearance(TaskViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.grey));
            holder.tvTaskDeadline.setTextColor(ContextCompat.getColor(context, R.color.grey));
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvTaskDeadline.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_on_surface));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTaskCompleted;
        TextView tvTaskTitle, tvTaskDeadline;
        ImageButton btnEditTask, btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTaskCompleted = itemView.findViewById(R.id.cb_task_completed);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDeadline = itemView.findViewById(R.id.tv_task_deadline);
            btnEditTask = itemView.findViewById(R.id.btn_edit_task);
            btnDeleteTask = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}
