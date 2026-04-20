package com.mobproj.habittracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobproj.habittracker.model.User;
import com.mobproj.habittracker.util.PasswordUtils;

public class UserDao {

    private final DatabaseHelper helper;

    public UserDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long register(String username, String plainPassword) {
        if (findByUsername(username) != null) {
            return -1;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_USERNAME, username.trim());
        cv.put(DatabaseHelper.COL_USER_PASSWORD, PasswordUtils.hash(plainPassword));
        cv.put(DatabaseHelper.COL_USER_CREATED_AT, System.currentTimeMillis());
        return db.insert(DatabaseHelper.TABLE_USERS, null, cv);
    }

    public User authenticate(String username, String plainPassword) {
        User user = findByUsername(username);
        if (user == null) return null;
        String hashed = PasswordUtils.hash(plainPassword);
        if (hashed.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public User findByUsername(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_USERNAME + "=?",
                new String[]{username.trim()}, null, null, null);
        try {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            c.close();
        }
    }

    public boolean changePassword(long userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (user == null) return false;
        if (!PasswordUtils.hash(currentPassword).equals(user.getPassword())) return false;
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_PASSWORD, PasswordUtils.hash(newPassword));
        int rows = db.update(DatabaseHelper.TABLE_USERS, cv,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public User findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            c.close();
        }
    }

    private User fromCursor(Cursor c) {
        User u = new User();
        u.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        u.setUsername(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)));
        u.setPassword(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)));
        u.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CREATED_AT)));
        return u;
    }
}
