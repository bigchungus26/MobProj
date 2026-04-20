package com.mobproj.habittracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobproj.habittracker.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitDao {

    private final DatabaseHelper helper;

    public HabitDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long insert(Habit habit) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = toContentValues(habit);
        cv.put(DatabaseHelper.COL_HABIT_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_HABITS, null, cv);
    }

    public int update(Habit habit) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_HABITS, toContentValues(habit),
                DatabaseHelper.COL_HABIT_ID + "=?",
                new String[]{String.valueOf(habit.getId())});
    }

    public int delete(long habitId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_HABITS,
                DatabaseHelper.COL_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)});
    }

    public List<Habit> findForUser(long userId) {
        return findForUser(userId, null);
    }

    public List<Habit> findForUser(long userId, Long categoryId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT h.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name FROM " +
                DatabaseHelper.TABLE_HABITS + " h LEFT JOIN " +
                DatabaseHelper.TABLE_CATEGORIES + " c ON h." +
                DatabaseHelper.COL_HABIT_CATEGORY_ID + " = c." + DatabaseHelper.COL_CAT_ID +
                " WHERE h." + DatabaseHelper.COL_HABIT_USER_ID + "=?";
        String[] args;
        if (categoryId != null) {
            sql += " AND h." + DatabaseHelper.COL_HABIT_CATEGORY_ID + "=?";
            args = new String[]{String.valueOf(userId), String.valueOf(categoryId)};
        } else {
            args = new String[]{String.valueOf(userId)};
        }
        sql += " ORDER BY h." + DatabaseHelper.COL_HABIT_NAME + " ASC";

        Cursor c = db.rawQuery(sql, args);
        List<Habit> list = new ArrayList<>();
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Habit findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT h.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name FROM " +
                DatabaseHelper.TABLE_HABITS + " h LEFT JOIN " +
                DatabaseHelper.TABLE_CATEGORIES + " c ON h." +
                DatabaseHelper.COL_HABIT_CATEGORY_ID + " = c." + DatabaseHelper.COL_CAT_ID +
                " WHERE h." + DatabaseHelper.COL_HABIT_ID + "=?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            c.close();
        }
    }

    public int countForUser(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_HABITS +
                " WHERE " + DatabaseHelper.COL_HABIT_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private ContentValues toContentValues(Habit habit) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_HABIT_USER_ID, habit.getUserId());
        if (habit.getCategoryId() != null) {
            cv.put(DatabaseHelper.COL_HABIT_CATEGORY_ID, habit.getCategoryId());
        } else {
            cv.putNull(DatabaseHelper.COL_HABIT_CATEGORY_ID);
        }
        cv.put(DatabaseHelper.COL_HABIT_NAME, habit.getName());
        cv.put(DatabaseHelper.COL_HABIT_DESCRIPTION, habit.getDescription());
        cv.put(DatabaseHelper.COL_HABIT_FREQUENCY, habit.getFrequency());
        cv.put(DatabaseHelper.COL_HABIT_REMINDER, habit.getReminderTime());
        return cv;
    }

    private Habit fromCursor(Cursor c) {
        Habit h = new Habit();
        h.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_ID)));
        h.setUserId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_USER_ID)));
        int catIdx = c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_CATEGORY_ID);
        h.setCategoryId(c.isNull(catIdx) ? null : c.getLong(catIdx));
        h.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_NAME)));
        h.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_DESCRIPTION)));
        h.setFrequency(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_FREQUENCY)));
        h.setReminderTime(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_REMINDER)));
        h.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_HABIT_CREATED_AT)));
        int nameIdx = c.getColumnIndex("cat_name");
        if (nameIdx != -1 && !c.isNull(nameIdx)) {
            h.setCategoryName(c.getString(nameIdx));
        }
        return h;
    }
}
