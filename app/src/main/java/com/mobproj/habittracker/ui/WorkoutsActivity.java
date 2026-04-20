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
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.data.WorkoutDao;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.model.Workout;
import com.mobproj.habittracker.ui.adapter.WorkoutAdapter;
import com.mobproj.habittracker.util.DateUtils;

import java.util.List;

public class WorkoutsActivity extends BaseActivity {

    private RecyclerView recycler;
    private TextView emptyState;
    private WorkoutAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_workouts);
        }

        recycler = findViewById(R.id.recycler_workouts);
        emptyState = findViewById(R.id.text_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditWorkoutActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void loadWorkouts() {
        long userId = session.getUserId();
        String today = DateUtils.todayIso();
        ProgressDao progressDao = new ProgressDao(this);
        List<Workout> workouts = new WorkoutDao(this).findForUser(userId);

        if (workouts.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        adapter = new WorkoutAdapter(workouts, new WorkoutAdapter.Listener() {
            @Override
            public boolean isCompletedToday(Workout workout) {
                return progressDao.isCompletedOn(userId, workout.getId(),
                        ProgressRecord.TYPE_WORKOUT, today);
            }

            @Override
            public void onToggle(Workout workout, boolean checked) {
                progressDao.upsertCompletion(userId, workout.getId(),
                        ProgressRecord.TYPE_WORKOUT, today, checked, null);
            }

            @Override
            public void onEdit(Workout workout) {
                Intent i = new Intent(WorkoutsActivity.this, AddEditWorkoutActivity.class);
                i.putExtra(AddEditWorkoutActivity.EXTRA_WORKOUT_ID, workout.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Workout workout) {
                new AlertDialog.Builder(WorkoutsActivity.this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(getString(R.string.confirm_delete_workout, workout.getName()))
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            new WorkoutDao(WorkoutsActivity.this).delete(workout.getId());
                            loadWorkouts();
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
