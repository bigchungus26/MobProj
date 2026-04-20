package com.mobproj.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.data.WorkoutDao;
import com.mobproj.habittracker.util.DateUtils;

public class MainActivity extends BaseActivity {

    private TextView greetingText;
    private TextView habitsCountText;
    private TextView workoutsCountText;
    private TextView completedTodayText;
    private TextView dailyGoalText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isSignedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        greetingText = findViewById(R.id.text_greeting);
        habitsCountText = findViewById(R.id.text_habits_count);
        workoutsCountText = findViewById(R.id.text_workouts_count);
        completedTodayText = findViewById(R.id.text_completed_today);
        dailyGoalText = findViewById(R.id.text_daily_goal);

        CardView habitsCard = findViewById(R.id.card_habits);
        CardView workoutsCard = findViewById(R.id.card_workouts);
        CardView progressCard = findViewById(R.id.card_progress);
        CardView categoriesCard = findViewById(R.id.card_categories);

        habitsCard.setOnClickListener(v ->
                startActivity(new Intent(this, HabitsActivity.class)));
        workoutsCard.setOnClickListener(v ->
                startActivity(new Intent(this, WorkoutsActivity.class)));
        progressCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));
        categoriesCard.setOnClickListener(v ->
                startActivity(new Intent(this, CategoriesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }

    private void refreshStats() {
        long userId = session.getUserId();
        greetingText.setText(getString(R.string.greeting_format, session.getUsername()));

        int habits = new HabitDao(this).countForUser(userId);
        int workouts = new WorkoutDao(this).countForUser(userId);
        int completedToday = new ProgressDao(this)
                .countCompletedOn(userId, DateUtils.todayIso());
        int dailyGoal = session.getDailyGoal();

        habitsCountText.setText(String.valueOf(habits));
        workoutsCountText.setText(String.valueOf(workouts));
        completedTodayText.setText(String.valueOf(completedToday));
        dailyGoalText.setText(getString(R.string.daily_goal_format,
                completedToday, dailyGoal));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            session.signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
