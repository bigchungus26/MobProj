# Smart Habit & Workout Tracker

Android application that lets users create, manage, and track daily habits and
workouts with categories, progress history, and customizable settings.

## Stack

- **Language:** Java (Android)
- **UI:** XML layouts, Material Components
- **Storage:** SQLite (via `SQLiteOpenHelper`)
- **User prefs:** `SharedPreferences`

## Core features

- Local user accounts with salted PBKDF2 password hashing
- Habits (daily/weekly) with reminders and categories
- Workouts with duration, intensity and categories
- Daily completion tracking + progress history (last 100 records)
- Custom categories per user with color labels
- Settings: theme (system/light/dark), reminders toggle, daily goal
- Persistent session via SharedPreferences

## Data model

| Table      | Purpose                                         |
|------------|-------------------------------------------------|
| users      | Account credentials                             |
| categories | Per-user labels for habits/workouts             |
| habits     | Habit definitions                               |
| workouts   | Workout definitions                             |
| progress   | Daily completion records for habits and workouts|

## Project layout

```
app/
  src/main/
    java/com/mobproj/habittracker/
      data/         SQLiteOpenHelper + DAOs
      model/        POJOs (User, Habit, Workout, Category, ProgressRecord)
      ui/           Activities and adapters
      util/         Session, date and password helpers
    res/            Layouts, drawables, strings, themes
```

## Build

Open the project in Android Studio (Arctic Fox or newer) and run on an
emulator or device with API 24+.
