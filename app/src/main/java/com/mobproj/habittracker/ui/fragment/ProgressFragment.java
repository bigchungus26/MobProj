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

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.ui.adapter.ProgressAdapter;
import com.mobproj.habittracker.ui.widget.WeeklyChartView;
import com.mobproj.habittracker.util.DateUtils;
import com.mobproj.habittracker.util.SessionManager;

import java.util.List;

public class ProgressFragment extends Fragment {

    private RecyclerView recycler;
    private View emptyState;
    private TextView summaryText;
    private WeeklyChartView chart;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        recycler = view.findViewById(R.id.recycler_progress);
        emptyState = view.findViewById(R.id.empty_state);
        summaryText = view.findViewById(R.id.text_summary);
        chart = view.findViewById(R.id.weekly_chart);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        long userId = session.getUserId();
        ProgressDao dao = new ProgressDao(requireContext());
        List<ProgressRecord> records = dao.findForUser(userId, 100);
        int todayCount = dao.countCompletedOn(userId, DateUtils.todayIso());
        summaryText.setText(getString(R.string.progress_summary_format,
                todayCount, session.getDailyGoal()));

        chart.setData(dao.countCompletedForLastDays(userId, 7));

        if (records.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recycler.setAdapter(new ProgressAdapter(records));
    }
}
