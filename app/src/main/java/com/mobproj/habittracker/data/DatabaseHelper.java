package com.mobproj.habittracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "habit_tracker.db";
    public static final int DB_VERSION = 1;

    // Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Categories
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_USER_ID = "user_id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_TYPE = "type"; // HABIT or WORKOUT
    public static final String COL_CAT_COLOR = "color";

    // Habits
    public static final String TABLE_HABITS = "habits";
    public static final String COL_HABIT_ID = "id";
    public static final String COL_HABIT_USER_ID = "user_id";
    public static final String COL_HABIT_CATEGORY_ID = "category_id";
    public static final String COL_HABIT_NAME = "name";
    public static final String COL_HABIT_DESCRIPTION = "description";
    public static final String COL_HABIT_FREQUENCY = "frequency"; // DAILY, WEEKLY
    public static final String COL_HABIT_REMINDER = "reminder_time";
    public static final String COL_HABIT_CREATED_AT = "created_at";

    // Workouts
    public static final String TABLE_WORKOUTS = "workouts";
    public static final String COL_WORKOUT_ID = "id";
    public static final String COL_WORKOUT_USER_ID = "user_id";
    public static final String COL_WORKOUT_CATEGORY_ID = "category_id";
    public static final String COL_WORKOUT_NAME = "name";
    public static final String COL_WORKOUT_DESCRIPTION = "description";
    public static final String COL_WORKOUT_DURATION = "duration_minutes";
    public static final String COL_WORKOUT_INTENSITY = "intensity"; // LOW, MEDIUM, HIGH
    public static final String COL_WORKOUT_CREATED_AT = "created_at";

    // Progress records
    public static final String TABLE_PROGRESS = "progress";
    public static final String COL_PROG_ID = "id";
    public static final String COL_PROG_USER_ID = "user_id";
    public static final String COL_PROG_ITEM_ID = "item_id";
    public static final String COL_PROG_ITEM_TYPE = "item_type"; // HABIT or WORKOUT
    public static final String COL_PROG_DATE = "date";
    public static final String COL_PROG_COMPLETED = "completed";
    public static final String COL_PROG_NOTES = "notes";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL, " +
                COL_USER_CREATED_AT + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_USER_ID + " INTEGER NOT NULL, " +
                COL_CAT_NAME + " TEXT NOT NULL, " +
                COL_CAT_TYPE + " TEXT NOT NULL, " +
                COL_CAT_COLOR + " TEXT, " +
                "FOREIGN KEY(" + COL_CAT_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_HABITS + " (" +
                COL_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HABIT_USER_ID + " INTEGER NOT NULL, " +
                COL_HABIT_CATEGORY_ID + " INTEGER, " +
                COL_HABIT_NAME + " TEXT NOT NULL, " +
                COL_HABIT_DESCRIPTION + " TEXT, " +
                COL_HABIT_FREQUENCY + " TEXT NOT NULL DEFAULT 'DAILY', " +
                COL_HABIT_REMINDER + " TEXT, " +
                COL_HABIT_CREATED_AT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_HABIT_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_HABIT_CATEGORY_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COL_CAT_ID + ") ON DELETE SET NULL)");

        db.execSQL("CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COL_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORKOUT_USER_ID + " INTEGER NOT NULL, " +
                COL_WORKOUT_CATEGORY_ID + " INTEGER, " +
                COL_WORKOUT_NAME + " TEXT NOT NULL, " +
                COL_WORKOUT_DESCRIPTION + " TEXT, " +
                COL_WORKOUT_DURATION + " INTEGER NOT NULL DEFAULT 0, " +
                COL_WORKOUT_INTENSITY + " TEXT NOT NULL DEFAULT 'MEDIUM', " +
                COL_WORKOUT_CREATED_AT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_WORKOUT_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_WORKOUT_CATEGORY_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COL_CAT_ID + ") ON DELETE SET NULL)");

        db.execSQL("CREATE TABLE " + TABLE_PROGRESS + " (" +
                COL_PROG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PROG_USER_ID + " INTEGER NOT NULL, " +
                COL_PROG_ITEM_ID + " INTEGER NOT NULL, " +
                COL_PROG_ITEM_TYPE + " TEXT NOT NULL, " +
                COL_PROG_DATE + " TEXT NOT NULL, " +
                COL_PROG_COMPLETED + " INTEGER NOT NULL DEFAULT 0, " +
                COL_PROG_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COL_PROG_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_progress_user_date ON " +
                TABLE_PROGRESS + "(" + COL_PROG_USER_ID + ", " + COL_PROG_DATE + ")");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
