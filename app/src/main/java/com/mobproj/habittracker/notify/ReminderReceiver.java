package com.mobproj.habittracker.notify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.model.Habit;
import com.mobproj.habittracker.ui.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";

    public static final String CHANNEL_ID = "habit_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L);
        String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);
        if (habitId == -1L) return;

        Habit habit = new HabitDao(context).findById(habitId);
        if (habit == null) return;
        if (habitName == null) habitName = habit.getName();

        createChannel(context);
        postNotification(context, habitId, habitName);
        ReminderScheduler.scheduleForHabit(context, habit);
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.notif_channel_reminders_desc));
        nm.createNotificationChannel(channel);
    }

    private void postNotification(Context context, long habitId, String habitName) {
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent tapPi = PendingIntent.getActivity(context,
                (int) (habitId & 0x7FFFFFFF), tapIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_reminder_title))
                .setContentText(context.getString(R.string.notif_reminder_body, habitName))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(tapPi);

        NotificationManager nm = ContextCompat.getSystemService(context, NotificationManager.class);
        if (nm == null) return;
        nm.notify((int) (habitId & 0x7FFFFFFF), builder.build());
    }
}
