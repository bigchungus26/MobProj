package com.mobproj.habittracker.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.data.WorkoutDao;
import com.mobproj.habittracker.model.Habit;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.model.Workout;
import com.mobproj.habittracker.ui.adapter.TodayAdapter;
import com.mobproj.habittracker.util.DateUtils;
import com.mobproj.habittracker.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private TextView dateText;
    private TextView progressLabel;
    private LinearProgressIndicator progressBar;
    private RecyclerView recycler;
    private View emptyState;

    private SessionManager session;
    private TodayAdapter adapter;
    private final List<TodayAdapter.Item> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        greetingText = view.findViewById(R.id.text_greeting);
        dateText = view.findViewById(R.id.text_date);
        progressLabel = view.findViewById(R.id.text_progress_label);
        progressBar = view.findViewById(R.id.progress_today);
        recycler = view.findViewById(R.id.recycler_today);
        emptyState = view.findViewById(R.id.empty_state);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TodayAdapter(items, this::onToggleItem);
        recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        long userId = session.getUserId();
        String today = DateUtils.todayIso();
        ProgressDao progressDao = new ProgressDao(requireContext());
        List<Habit> habits = new HabitDao(requireContext()).findForUser(userId);
        List<Workout> workouts = new WorkoutDao(requireContext()).findForUser(userId);

        items.clear();
        for (Habit h : habits) {
            boolean done = progressDao.isCompletedOn(userId, h.getId(),
                    ProgressRecord.TYPE_HABIT, today);
            items.add(new TodayAdapter.Item(h.getId(), ProgressRecord.TYPE_HABIT,
                    h.getName(), buildSubtitle(h), done));
        }
        for (Workout w : workouts) {
            boolean done = progressDao.isCompletedOn(userId, w.getId(),
                    ProgressRecord.TYPE_WORKOUT, today);
            items.add(new TodayAdapter.Item(w.getId(), ProgressRecord.TYPE_WORKOUT,
                    w.getName(), buildSubtitle(w), done));
        }

        greetingText.setText(getString(R.string.greeting_format, session.getUsername()));
        dateText.setText(new SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                .format(new Date()));

        int completed = progressDao.countCompletedOn(userId, today);
        int total = items.size();
        progressBar.setMax(Math.max(total, 1));
        progressBar.setProgress(completed);
        progressLabel.setText(total == 0
                ? getString(R.string.today_empty)
                : getString(R.string.daily_goal_format, completed, total));

        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private String buildSubtitle(Habit h) {
        StringBuilder sb = new StringBuilder(h.getFrequency());
        if (h.getCategoryName() != null) sb.insert(0, h.getCategoryName() + " • ");
        if (h.getReminderTime() != null) sb.append(" • ").append(h.getReminderTime());
        return sb.toString();
    }

    private String buildSubtitle(Workout w) {
        StringBuilder sb = new StringBuilder();
        if (w.getCategoryName() != null) sb.append(w.getCategoryName()).append(" • ");
        sb.append(w.getDurationMinutes()).append(" min • ").append(w.getIntensity());
        return sb.toString();
    }

    private void onToggleItem(TodayAdapter.Item item, boolean checked) {
        new ProgressDao(requireContext()).upsertCompletion(
                session.getUserId(), item.id, item.type,
                DateUtils.todayIso(), checked, null);
        refresh();
    }
}
