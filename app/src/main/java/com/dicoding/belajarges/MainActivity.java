package com.dicoding.belajarges;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dicoding.belajarges.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements CourseAdapter.CourseListener {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    private CourseAdapter courseAdapter;
    private final List<Course> courseList = new ArrayList<>();
    private final List<Task> allTasks = new ArrayList<>();

    // Listener untuk mencegah kebocoran data saat activity dihancurkan
    private ListenerRegistration courseListener;
    private ListenerRegistration taskListener;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Izin notifikasi ditolak. Fitur pengingat tidak akan berfungsi.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Jika karena suatu hal user null, kembali ke login
            goToLoginActivity();
            return;
        }

        // Konfigurasi Google Client untuk proses logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupRecyclerView();
        askNotificationPermission();

        binding.fabAddCourse.setOnClickListener(v -> showCourseDialog(null));

        // Memuat data setelah user dipastikan ada
        loadData();
    }

    private void loadData() {
        if (currentUser != null) {
            fetchCourses();
            fetchTasks();
        }
    }

    // --- Menu untuk Logout ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show();
            goToLoginActivity();
        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- Akhir Menu Logout ---

    private void fetchCourses() {
        // Path diubah menjadi spesifik untuk user
        CollectionReference coursesRef = db.collection("users").document(currentUser.getUid()).collection("courses");
        courseListener = coursesRef.orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Gagal memuat mata kuliah: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Course course = dc.getDocument().toObject(Course.class);
                        course.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                courseList.add(course);
                                break;
                            case MODIFIED:
                                int index = findCourseIndexById(course.getId());
                                if (index != -1) {
                                    course.setTasks(courseList.get(index).getTasks()); // Pertahankan list tugas
                                    courseList.set(index, course);
                                }
                                break;
                            case REMOVED:
                                int removeIndex = findCourseIndexById(course.getId());
                                if (removeIndex != -1) {
                                    courseList.remove(removeIndex);
                                }
                                break;
                        }
                    }
                    distributeTasksToCourses();
                });
    }

    private void fetchTasks() {
        // Path diubah menjadi spesifik untuk user
        CollectionReference tasksRef = db.collection("users").document(currentUser.getUid()).collection("tasks");
        taskListener = tasksRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Gagal memuat tugas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snapshots == null) return;

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                Task task = dc.getDocument().toObject(Task.class);
                task.setId(dc.getDocument().getId());

                switch (dc.getType()) {
                    case ADDED:
                        allTasks.add(task);
                        break;
                    case MODIFIED:
                        int index = findTaskIndexById(task.getId());
                        if (index != -1) {
                            allTasks.set(index, task);
                        }
                        break;
                    case REMOVED:
                        int removeIndex = findTaskIndexById(task.getId());
                        if (removeIndex != -1) {
                            allTasks.remove(removeIndex);
                        }
                        break;
                }
            }
            distributeTasksToCourses();
        });
    }

    private void saveCourse(Course course, String name) {
        CollectionReference coursesRef = db.collection("users").document(currentUser.getUid()).collection("courses");
        if (course == null) { // Tambah baru
            Course newCourse = new Course(null, name);
            coursesRef.add(newCourse)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Mata kuliah ditambahkan", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menambahkan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else { // Edit
            coursesRef.document(course.getId()).update("name", name)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mata kuliah diperbarui", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveTask(Task task, Course course, String title, long deadline) {
        CollectionReference tasksRef = db.collection("users").document(currentUser.getUid()).collection("tasks");
        if (task == null) { // Tambah baru
            Task newTask = new Task(null, course.getId(), title, deadline, false);
            tasksRef.add(newTask)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Tugas ditambahkan", Toast.LENGTH_SHORT).show();
                        newTask.setId(docRef.getId());
                        setReminder(newTask);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menambahkan tugas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else { // Edit
            tasksRef.document(task.getId())
                    .update("title", title, "deadline", deadline)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show();
                        task.setTitle(title);
                        task.setDeadline(deadline);
                        setReminder(task); // Setel ulang reminder
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui tugas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        db.collection("users").document(currentUser.getUid()).collection("tasks")
                .document(task.getId()).update("completed", isChecked);
        if (isChecked) {
            cancelReminder(task);
        } else {
            setReminder(task);
        }
    }

    @Override
    public void onDeleteCourse(Course course) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Mata Kuliah")
                .setMessage("Apakah Anda yakin ingin menghapus mata kuliah '" + course.getName() + "'? Semua tugas di dalamnya juga akan terhapus.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    WriteBatch batch = db.batch();
                    CollectionReference tasksRef = db.collection("users").document(currentUser.getUid()).collection("tasks");
                    CollectionReference coursesRef = db.collection("users").document(currentUser.getUid()).collection("courses");

                    // Hapus semua tugas terkait
                    List<Task> tasksToDelete = allTasks.stream()
                            .filter(t -> t.getCourseId().equals(course.getId()))
                            .collect(Collectors.toList());

                    for (Task task : tasksToDelete) {
                        cancelReminder(task);
                        batch.delete(tasksRef.document(task.getId()));
                    }

                    // Hapus mata kuliahnya
                    batch.delete(coursesRef.document(course.getId()));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mata kuliah dan tugas dihapus", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onDeleteTask(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Tugas")
                .setMessage("Yakin ingin menghapus tugas '" + task.getTitle() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    cancelReminder(task);
                    db.collection("users").document(currentUser.getUid()).collection("tasks")
                            .document(task.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tugas dihapus", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hapus listener untuk menghindari memory leak
        if (courseListener != null) {
            courseListener.remove();
        }
        if (taskListener != null) {
            taskListener.remove();
        }
    }

    // --- Sisa fungsi (tidak berubah signifikan, hanya path) ---
    // ... (Fungsi seperti setupRecyclerView, askNotificationPermission, showCourseDialog, showTaskDialog,
    //      distributeTasksToCourses, findCourseIndexById, findTaskIndexById, setReminder, cancelReminder, dll.
    //      tetap sama seperti kode sebelumnya, karena path sudah diperbaiki di fungsi utamanya)
    // --- Anda bisa salin sisa fungsi dari kode sebelumnya ke sini ---
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(this, courseList, this);
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCourses.setAdapter(courseAdapter);
    }

    private void distributeTasksToCourses() {
        for (Course course : courseList) {
            List<Task> tasksForCourse = allTasks.stream()
                    .filter(task -> task.getCourseId().equals(course.getId()))
                    .sorted((t1, t2) -> Long.compare(t1.getDeadline(), t2.getDeadline()))
                    .collect(Collectors.toList());
            course.setTasks(tasksForCourse);
        }
        courseAdapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (courseList.isEmpty()) {
            binding.rvCourses.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvCourses.setVisibility(View.VISIBLE);
            binding.tvEmpty.setVisibility(View.GONE);
        }
    }

    private int findCourseIndexById(String courseId) {
        for (int i = 0; i < courseList.size(); i++) {
            if (courseList.get(i).getId().equals(courseId)) {
                return i;
            }
        }
        return -1;
    }

    private int findTaskIndexById(String taskId) {
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId().equals(taskId)) {
                return i;
            }
        }
        return -1;
    }

    private void showCourseDialog(final Course course) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        final EditText etCourseName = dialogView.findViewById(R.id.et_course_name);
        final TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);

        if (course != null) {
            tvDialogTitle.setText("Edit Mata Kuliah");
            etCourseName.setText(course.getName());
        } else {
            tvDialogTitle.setText("Tambah Mata Kuliah");
        }

        builder.setPositiveButton(course != null ? "Simpan" : "Tambah", (dialog, which) -> {
            String courseName = etCourseName.getText().toString().trim();
            if (TextUtils.isEmpty(courseName)) {
                Toast.makeText(this, "Nama mata kuliah tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            saveCourse(course, courseName);
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showTaskDialog(final Task task, final Course course) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText etTaskTitle = dialogView.findViewById(R.id.et_task_title);
        final Button btnDeadline = dialogView.findViewById(R.id.btn_deadline);
        final TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);

        final Calendar calendar = Calendar.getInstance();
        if (task != null) {
            calendar.setTimeInMillis(task.getDeadline());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        btnDeadline.setText(sdf.format(calendar.getTime()));

        btnDeadline.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    btnDeadline.setText(sdf.format(calendar.getTime()));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        if (task != null) {
            tvDialogTitle.setText("Edit Tugas");
            etTaskTitle.setText(task.getTitle());
        } else {
            tvDialogTitle.setText("Tambah Tugas");
        }

        builder.setPositiveButton(task != null ? "Simpan" : "Tambah", (dialog, which) -> {
            String taskTitle = etTaskTitle.getText().toString().trim();
            if (TextUtils.isEmpty(taskTitle)) {
                Toast.makeText(this, "Judul tugas tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            saveTask(task, course, taskTitle, calendar.getTimeInMillis());
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onAddTask(Course course) {
        showTaskDialog(null, course);
    }

    @Override
    public void onEditCourse(Course course) {
        showCourseDialog(course);
    }

    @Override
    public void onEditTask(Task task) {
        Course parentCourse = courseList.stream()
                .filter(c -> c.getId().equals(task.getCourseId()))
                .findFirst().orElse(null);
        if (parentCourse != null) {
            showTaskDialog(task, parentCourse);
        }
    }

    private void setReminder(Task task) {
        if (task.isCompleted() || task.getDeadline() <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.getTitle());
        int notificationId = Objects.hash(task.getId());
        intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = task.getDeadline() - (60 * 60 * 1000); // 1 jam sebelum

        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    private void cancelReminder(Task task) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        int notificationId = Objects.hash(task.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
