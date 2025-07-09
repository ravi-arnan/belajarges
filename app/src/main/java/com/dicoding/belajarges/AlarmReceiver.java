package com.dicoding.belajarges;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    private static final String CHANNEL_ID = "channel_deadline";
    private static final String CHANNEL_NAME = "Deadline Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        // Intent untuk membuka aplikasi saat notifikasi diklik
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Buat Notification Channel untuk Android Oreo ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel untuk pengingat deadline tugas");
            notificationManager.createNotificationChannel(channel);
        }

        // Buat notifikasi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ganti dengan ikon notifikasi Anda
                .setContentTitle("Pengingat Deadline!")
                .setContentText("Jangan lupa, tugas \"" + taskTitle + "\" akan segera berakhir.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Jangan lupa, tugas \"" + taskTitle + "\" akan segera berakhir."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}
