package com.mobproj.habittracker.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.ProgressDao;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.ui.adapter.ProgressAdapter;
import com.mobproj.habittracker.util.DateUtils;

import java.util.List;

public class ProgressActivity extends BaseActivity {

    private RecyclerView recycler;
    private TextView emptyState;
    private TextView summaryText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_progress);
        }

        recycler = findViewById(R.id.recycler_progress);
        emptyState = findViewById(R.id.text_empty);
        summaryText = findViewById(R.id.text_summary);

        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProgress();
    }

    private void loadProgress() {
        long userId = session.getUserId();
        ProgressDao dao = new ProgressDao(this);
        List<ProgressRecord> records = dao.findForUser(userId, 100);
        int todayCount = dao.countCompletedOn(userId, DateUtils.todayIso());
        summaryText.setText(getString(R.string.progress_summary_format,
                todayCount, session.getDailyGoal()));

        if (records.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        recycler.setAdapter(new ProgressAdapter(records));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
