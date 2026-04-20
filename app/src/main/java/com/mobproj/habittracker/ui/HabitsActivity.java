package com.mobproj.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.model.Habit;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.ui.adapter.HabitAdapter;
import com.mobproj.habittracker.util.DateUtils;

import java.util.List;

public class HabitsActivity extends BaseActivity {

    private RecyclerView recycler;
    private TextView emptyState;
    private HabitAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_habits);
        }

        recycler = findViewById(R.id.recycler_habits);
        emptyState = findViewById(R.id.text_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditHabitActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    private void loadHabits() {
        long userId = session.getUserId();
        String today = DateUtils.todayIso();
        ProgressDao progressDao = new ProgressDao(this);
        List<Habit> habits = new HabitDao(this).findForUser(userId);

        if (habits.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        adapter = new HabitAdapter(habits, new HabitAdapter.Listener() {
            @Override
            public boolean isCompletedToday(Habit habit) {
                return progressDao.isCompletedOn(userId, habit.getId(),
                        ProgressRecord.TYPE_HABIT, today);
            }

            @Override
            public void onToggle(Habit habit, boolean checked) {
                progressDao.upsertCompletion(userId, habit.getId(),
                        ProgressRecord.TYPE_HABIT, today, checked, null);
            }

            @Override
            public void onEdit(Habit habit) {
                Intent i = new Intent(HabitsActivity.this, AddEditHabitActivity.class);
                i.putExtra(AddEditHabitActivity.EXTRA_HABIT_ID, habit.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Habit habit) {
                new AlertDialog.Builder(HabitsActivity.this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(getString(R.string.confirm_delete_habit, habit.getName()))
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            new HabitDao(HabitsActivity.this).delete(habit.getId());
                            loadHabits();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
