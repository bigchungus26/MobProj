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
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.model.Habit;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.ui.AddEditHabitActivity;
import com.mobproj.habittracker.ui.adapter.HabitAdapter;
import com.mobproj.habittracker.util.DateUtils;
import com.mobproj.habittracker.util.SessionManager;

import java.util.List;

public class HabitsFragment extends Fragment {

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
        emptyText.setText(R.string.empty_habits);
        emptyIcon.setImageResource(R.drawable.ic_empty_habits);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setContentDescription(getString(R.string.action_add_habit));
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditHabitActivity.class)));
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
        List<Habit> habits = new HabitDao(requireContext()).findForUser(userId);

        if (habits.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        recycler.setAdapter(new HabitAdapter(habits, new HabitAdapter.Listener() {
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
                Intent i = new Intent(requireContext(), AddEditHabitActivity.class);
                i.putExtra(AddEditHabitActivity.EXTRA_HABIT_ID, habit.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Habit habit) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(getString(R.string.confirm_delete_habit, habit.getName()))
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            new HabitDao(requireContext()).delete(habit.getId());
                            load();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }));
    }
}
