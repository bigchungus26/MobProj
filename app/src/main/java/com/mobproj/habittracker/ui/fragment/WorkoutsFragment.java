package com.mobproj.habittracker.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.data.WorkoutDao;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.model.Workout;
import com.mobproj.habittracker.ui.AddEditWorkoutActivity;
import com.mobproj.habittracker.ui.adapter.WorkoutAdapter;
import com.mobproj.habittracker.util.DateUtils;
import com.mobproj.habittracker.util.SessionManager;

import java.util.List;

public class WorkoutsFragment extends Fragment {

    private RecyclerView recycler;
    private View emptyState;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        recycler = view.findViewById(R.id.recycler);
        emptyState = view.findViewById(R.id.empty_state);
        TextView emptyText = view.findViewById(R.id.text_empty);
        ImageView emptyIcon = view.findViewById(R.id.empty_icon);
        emptyText.setText(R.string.empty_workouts);
        emptyIcon.setImageResource(R.drawable.ic_empty_workouts);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setContentDescription(getString(R.string.action_add_workout));
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditWorkoutActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        long userId = session.getUserId();
        String today = DateUtils.todayIso();
        ProgressDao progressDao = new ProgressDao(requireContext());
        List<Workout> workouts = new WorkoutDao(requireContext()).findForUser(userId);

        if (workouts.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        recycler.setAdapter(new WorkoutAdapter(workouts, new WorkoutAdapter.Listener() {
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
                Intent i = new Intent(requireContext(), AddEditWorkoutActivity.class);
                i.putExtra(AddEditWorkoutActivity.EXTRA_WORKOUT_ID, workout.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Workout workout) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(getString(R.string.confirm_delete_workout, workout.getName()))
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            new WorkoutDao(requireContext()).delete(workout.getId());
                            load();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }));
    }
}
