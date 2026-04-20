package com.mobproj.habittracker.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    public static final String PREFS = "habit_tracker_prefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_THEME = "theme"; // LIGHT, DARK, SYSTEM
    public static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    public static final String KEY_DAILY_GOAL = "daily_goal";
    public static final String KEY_FIRST_DAY_OF_WEEK = "first_day_of_week";

    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";
    public static final String THEME_SYSTEM = "SYSTEM";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void signIn(long userId, String username) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public void signOut() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
    }

    public boolean isSignedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_SYSTEM);
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean areRemindersEnabled() {
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true);
    }

    public void setRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply();
    }

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 3);
    }

    public void setDailyGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply();
    }
}
