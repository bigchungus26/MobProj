package com.mobproj.habittracker.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.model.Habit;
import com.mobproj.habittracker.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public final class ReminderScheduler {

    private ReminderScheduler() {}

    public static void rescheduleAllForCurrentUser(Context context) {
        SessionManager session = new SessionManager(context);
        if (!session.isSignedIn()) return;

        cancelAllForUser(context, session.getUserId());
        if (!session.areRemindersEnabled()) return;

        List<Habit> habits = new HabitDao(context).findForUser(session.getUserId());
        for (Habit h : habits) {
            scheduleForHabit(context, h);
        }
    }

    public static void scheduleForHabit(Context context, Habit habit) {
        SessionManager session = new SessionManager(context);
        if (!session.areRemindersEnabled()) return;
        if (habit.getReminderTime() == null || !habit.getReminderTime().contains(":")) return;

        int[] hm = parseTime(habit.getReminderTime());
        if (hm == null) return;

        long triggerAt = nextTrigger(hm[0], hm[1]);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pendingIntentFor(context, habit.getId(), habit.getName(), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void cancelForHabit(Context context, long habitId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pendingIntentFor(context, habitId, null, false);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }

    public static void cancelAllForUser(Context context, long userId) {
        List<Habit> habits = new HabitDao(context).findForUser(userId);
        for (Habit h : habits) {
            cancelForHabit(context, h.getId());
        }
    }

    private static int[] parseTime(String hhmm) {
        try {
            String[] parts = hhmm.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return new int[]{h, m};
        } catch (Exception e) {
            return null;
        }
    }

    private static long nextTrigger(int hour, int minute) {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        if (target.getTimeInMillis() <= System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }
        return target.getTimeInMillis();
    }

    private static PendingIntent pendingIntentFor(Context context, long habitId,
                                                  String habitName, boolean create) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId);
        if (habitName != null) {
            intent.putExtra(ReminderReceiver.EXTRA_HABIT_NAME, habitName);
        }
        int requestCode = (int) (habitId & 0x7FFFFFFF);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        if (!create) {
            flags |= PendingIntent.FLAG_NO_CREATE;
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }
}
