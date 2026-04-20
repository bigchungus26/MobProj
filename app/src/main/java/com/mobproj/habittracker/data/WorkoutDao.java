package com.mobproj.habittracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobproj.habittracker.model.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDao {

    private final DatabaseHelper helper;

    public WorkoutDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long insert(Workout workout) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = toContentValues(workout);
        cv.put(DatabaseHelper.COL_WORKOUT_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_WORKOUTS, null, cv);
    }

    public int update(Workout workout) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_WORKOUTS, toContentValues(workout),
                DatabaseHelper.COL_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workout.getId())});
    }

    public int delete(long workoutId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_WORKOUTS,
                DatabaseHelper.COL_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workoutId)});
    }

    public List<Workout> findForUser(long userId) {
        return findForUser(userId, null);
    }

    public List<Workout> findForUser(long userId, Long categoryId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT w.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name FROM " +
                DatabaseHelper.TABLE_WORKOUTS + " w LEFT JOIN " +
                DatabaseHelper.TABLE_CATEGORIES + " c ON w." +
                DatabaseHelper.COL_WORKOUT_CATEGORY_ID + " = c." + DatabaseHelper.COL_CAT_ID +
                " WHERE w." + DatabaseHelper.COL_WORKOUT_USER_ID + "=?";
        String[] args;
        if (categoryId != null) {
            sql += " AND w." + DatabaseHelper.COL_WORKOUT_CATEGORY_ID + "=?";
            args = new String[]{String.valueOf(userId), String.valueOf(categoryId)};
        } else {
            args = new String[]{String.valueOf(userId)};
        }
        sql += " ORDER BY w." + DatabaseHelper.COL_WORKOUT_NAME + " ASC";

        Cursor c = db.rawQuery(sql, args);
        List<Workout> list = new ArrayList<>();
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Workout findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT w.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name FROM " +
                DatabaseHelper.TABLE_WORKOUTS + " w LEFT JOIN " +
                DatabaseHelper.TABLE_CATEGORIES + " c ON w." +
                DatabaseHelper.COL_WORKOUT_CATEGORY_ID + " = c." + DatabaseHelper.COL_CAT_ID +
                " WHERE w." + DatabaseHelper.COL_WORKOUT_ID + "=?";
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
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_WORKOUTS +
                " WHERE " + DatabaseHelper.COL_WORKOUT_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private ContentValues toContentValues(Workout workout) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_WORKOUT_USER_ID, workout.getUserId());
        if (workout.getCategoryId() != null) {
            cv.put(DatabaseHelper.COL_WORKOUT_CATEGORY_ID, workout.getCategoryId());
        } else {
            cv.putNull(DatabaseHelper.COL_WORKOUT_CATEGORY_ID);
        }
        cv.put(DatabaseHelper.COL_WORKOUT_NAME, workout.getName());
        cv.put(DatabaseHelper.COL_WORKOUT_DESCRIPTION, workout.getDescription());
        cv.put(DatabaseHelper.COL_WORKOUT_DURATION, workout.getDurationMinutes());
        cv.put(DatabaseHelper.COL_WORKOUT_INTENSITY, workout.getIntensity());
        return cv;
    }

    private Workout fromCursor(Cursor c) {
        Workout w = new Workout();
        w.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_ID)));
        w.setUserId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_USER_ID)));
        int catIdx = c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_CATEGORY_ID);
        w.setCategoryId(c.isNull(catIdx) ? null : c.getLong(catIdx));
        w.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_NAME)));
        w.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DESCRIPTION)));
        w.setDurationMinutes(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION)));
        w.setIntensity(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_INTENSITY)));
        w.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_CREATED_AT)));
        int nameIdx = c.getColumnIndex("cat_name");
        if (nameIdx != -1 && !c.isNull(nameIdx)) {
            w.setCategoryName(c.getString(nameIdx));
        }
        return w;
    }
}
