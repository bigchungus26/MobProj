package com.mobproj.habittracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobproj.habittracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private final DatabaseHelper helper;

    public CategoryDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long insert(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_CAT_USER_ID, category.getUserId());
        cv.put(DatabaseHelper.COL_CAT_NAME, category.getName());
        cv.put(DatabaseHelper.COL_CAT_TYPE, category.getType());
        cv.put(DatabaseHelper.COL_CAT_COLOR, category.getColor());
        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
    }

    public int update(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_CAT_NAME, category.getName());
        cv.put(DatabaseHelper.COL_CAT_TYPE, category.getType());
        cv.put(DatabaseHelper.COL_CAT_COLOR, category.getColor());
        return db.update(DatabaseHelper.TABLE_CATEGORIES, cv,
                DatabaseHelper.COL_CAT_ID + "=?",
                new String[]{String.valueOf(category.getId())});
    }

    public int delete(long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COL_CAT_ID + "=?",
                new String[]{String.valueOf(categoryId)});
    }

    public List<Category> findAllForUser(long userId) {
        return query(DatabaseHelper.COL_CAT_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public List<Category> findByType(long userId, String type) {
        return query(DatabaseHelper.COL_CAT_USER_ID + "=? AND " +
                        DatabaseHelper.COL_CAT_TYPE + "=?",
                new String[]{String.valueOf(userId), type});
    }

    public Category findById(long id) {
        List<Category> list = query(DatabaseHelper.COL_CAT_ID + "=?",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public void ensureDefaults(long userId) {
        if (!findAllForUser(userId).isEmpty()) return;
        String[][] defaults = {
                {"Health", Category.TYPE_HABIT, "#4CAF50"},
                {"Productivity", Category.TYPE_HABIT, "#2196F3"},
                {"Mindfulness", Category.TYPE_HABIT, "#9C27B0"},
                {"Cardio", Category.TYPE_WORKOUT, "#F44336"},
                {"Strength", Category.TYPE_WORKOUT, "#FF9800"},
                {"Flexibility", Category.TYPE_WORKOUT, "#00BCD4"}
        };
        for (String[] d : defaults) {
            Category c = new Category();
            c.setUserId(userId);
            c.setName(d[0]);
            c.setType(d[1]);
            c.setColor(d[2]);
            insert(c);
        }
    }

    private List<Category> query(String where, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                where, args, null, null,
                DatabaseHelper.COL_CAT_NAME + " ASC");
        List<Category> list = new ArrayList<>();
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    private Category fromCursor(Cursor c) {
        Category cat = new Category();
        cat.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)));
        cat.setUserId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_USER_ID)));
        cat.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME)));
        cat.setType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_TYPE)));
        cat.setColor(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_COLOR)));
        return cat;
    }
}
