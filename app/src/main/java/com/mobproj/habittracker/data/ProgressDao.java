package com.mobproj.habittracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobproj.habittracker.model.ProgressRecord;

import java.util.ArrayList;
import java.util.List;

public class ProgressDao {

    private final DatabaseHelper helper;

    public ProgressDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long insert(ProgressRecord record) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PROG_USER_ID, record.getUserId());
        cv.put(DatabaseHelper.COL_PROG_ITEM_ID, record.getItemId());
        cv.put(DatabaseHelper.COL_PROG_ITEM_TYPE, record.getItemType());
        cv.put(DatabaseHelper.COL_PROG_DATE, record.getDate());
        cv.put(DatabaseHelper.COL_PROG_COMPLETED, record.isCompleted() ? 1 : 0);
        cv.put(DatabaseHelper.COL_PROG_NOTES, record.getNotes());
        return db.insert(DatabaseHelper.TABLE_PROGRESS, null, cv);
    }

    public long upsertCompletion(long userId, long itemId, String itemType,
                                 String date, boolean completed, String notes) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PROG_COMPLETED, completed ? 1 : 0);
        if (notes != null) cv.put(DatabaseHelper.COL_PROG_NOTES, notes);

        String where = DatabaseHelper.COL_PROG_USER_ID + "=? AND " +
                DatabaseHelper.COL_PROG_ITEM_ID + "=? AND " +
                DatabaseHelper.COL_PROG_ITEM_TYPE + "=? AND " +
                DatabaseHelper.COL_PROG_DATE + "=?";
        String[] args = {String.valueOf(userId), String.valueOf(itemId), itemType, date};
        int updated = db.update(DatabaseHelper.TABLE_PROGRESS, cv, where, args);
        if (updated > 0) return updated;

        cv.put(DatabaseHelper.COL_PROG_USER_ID, userId);
        cv.put(DatabaseHelper.COL_PROG_ITEM_ID, itemId);
        cv.put(DatabaseHelper.COL_PROG_ITEM_TYPE, itemType);
        cv.put(DatabaseHelper.COL_PROG_DATE, date);
        return db.insert(DatabaseHelper.TABLE_PROGRESS, null, cv);
    }

    public boolean isCompletedOn(long userId, long itemId, String itemType, String date) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PROGRESS,
                new String[]{DatabaseHelper.COL_PROG_COMPLETED},
                DatabaseHelper.COL_PROG_USER_ID + "=? AND " +
                        DatabaseHelper.COL_PROG_ITEM_ID + "=? AND " +
                        DatabaseHelper.COL_PROG_ITEM_TYPE + "=? AND " +
                        DatabaseHelper.COL_PROG_DATE + "=?",
                new String[]{String.valueOf(userId), String.valueOf(itemId), itemType, date},
                null, null, null);
        try {
            return c.moveToFirst() && c.getInt(0) == 1;
        } finally {
            c.close();
        }
    }

    public int delete(long recordId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_PROGRESS,
                DatabaseHelper.COL_PROG_ID + "=?",
                new String[]{String.valueOf(recordId)});
    }

    public List<ProgressRecord> findForUser(long userId, int limit) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT p.*, " +
                "CASE p." + DatabaseHelper.COL_PROG_ITEM_TYPE +
                " WHEN 'HABIT' THEN h." + DatabaseHelper.COL_HABIT_NAME +
                " WHEN 'WORKOUT' THEN w." + DatabaseHelper.COL_WORKOUT_NAME +
                " END AS item_name " +
                "FROM " + DatabaseHelper.TABLE_PROGRESS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_HABITS + " h ON p." +
                DatabaseHelper.COL_PROG_ITEM_ID + "=h." + DatabaseHelper.COL_HABIT_ID +
                " AND p." + DatabaseHelper.COL_PROG_ITEM_TYPE + "='HABIT' " +
                "LEFT JOIN " + DatabaseHelper.TABLE_WORKOUTS + " w ON p." +
                DatabaseHelper.COL_PROG_ITEM_ID + "=w." + DatabaseHelper.COL_WORKOUT_ID +
                " AND p." + DatabaseHelper.COL_PROG_ITEM_TYPE + "='WORKOUT' " +
                "WHERE p." + DatabaseHelper.COL_PROG_USER_ID + "=? " +
                "ORDER BY p." + DatabaseHelper.COL_PROG_DATE + " DESC, p." +
                DatabaseHelper.COL_PROG_ID + " DESC LIMIT ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), String.valueOf(limit)});
        List<ProgressRecord> list = new ArrayList<>();
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public int countCompletedOn(long userId, String date) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PROGRESS +
                " WHERE " + DatabaseHelper.COL_PROG_USER_ID + "=? AND " +
                DatabaseHelper.COL_PROG_DATE + "=? AND " +
                DatabaseHelper.COL_PROG_COMPLETED + "=1",
                new String[]{String.valueOf(userId), date});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public int currentStreak(long userId, long itemId, String itemType, String todayIso) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_PROGRESS,
                new String[]{DatabaseHelper.COL_PROG_DATE},
                DatabaseHelper.COL_PROG_USER_ID + "=? AND " +
                        DatabaseHelper.COL_PROG_ITEM_ID + "=? AND " +
                        DatabaseHelper.COL_PROG_ITEM_TYPE + "=? AND " +
                        DatabaseHelper.COL_PROG_COMPLETED + "=1",
                new String[]{String.valueOf(userId), String.valueOf(itemId), itemType},
                null, null, DatabaseHelper.COL_PROG_DATE + " DESC");
        int streak = 0;
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd", java.util.Locale.US);
            java.util.Calendar cursor = java.util.Calendar.getInstance();
            cursor.setTime(fmt.parse(todayIso));
            while (c.moveToNext()) {
                String date = c.getString(0);
                String expected = fmt.format(cursor.getTime());
                if (date.equals(expected)) {
                    streak++;
                    cursor.add(java.util.Calendar.DAY_OF_MONTH, -1);
                } else {
                    break;
                }
            }
        } catch (java.text.ParseException e) {
            return 0;
        } finally {
            c.close();
        }
        return streak;
    }

    private ProgressRecord fromCursor(Cursor c) {
        ProgressRecord r = new ProgressRecord();
        r.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_ID)));
        r.setUserId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_USER_ID)));
        r.setItemId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_ITEM_ID)));
        r.setItemType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_ITEM_TYPE)));
        r.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_DATE)));
        r.setCompleted(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_COMPLETED)) == 1);
        r.setNotes(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROG_NOTES)));
        int nameIdx = c.getColumnIndex("item_name");
        if (nameIdx != -1 && !c.isNull(nameIdx)) {
            r.setItemName(c.getString(nameIdx));
        }
        return r;
    }
}
