package com.mobproj.habittracker.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.notify.ReminderScheduler;
import com.mobproj.habittracker.ui.fragment.HabitsFragment;
import com.mobproj.habittracker.ui.fragment.HomeFragment;
import com.mobproj.habittracker.ui.fragment.ProgressFragment;
import com.mobproj.habittracker.ui.fragment.WorkoutsFragment;

public class MainActivity extends BaseActivity {

    private static final String STATE_SELECTED_NAV = "selected_nav";

    private BottomNavigationView bottomNav;
    private int currentNavId = R.id.nav_home;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { /* silently continue either way */ });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isSignedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_nav);
        if (savedInstanceState != null) {
            currentNavId = savedInstanceState.getInt(STATE_SELECTED_NAV, R.id.nav_home);
        } else {
            showFragment(currentNavId);
        }
        bottomNav.setSelectedItemId(currentNavId);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() != currentNavId) {
                showFragment(item.getItemId());
            }
            return true;
        });

        ensureNotificationPermission();
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (!session.areRemindersEnabled()) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void showFragment(int navId) {
        currentNavId = navId;
        Fragment fragment;
        int titleRes;
        if (navId == R.id.nav_habits) {
            fragment = new HabitsFragment();
            titleRes = R.string.title_habits;
        } else if (navId == R.id.nav_workouts) {
            fragment = new WorkoutsFragment();
            titleRes = R.string.title_workouts;
        } else if (navId == R.id.nav_progress) {
            fragment = new ProgressFragment();
            titleRes = R.string.title_progress;
        } else {
            fragment = new HomeFragment();
            titleRes = R.string.app_name;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleRes);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_NAV, currentNavId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_categories) {
            startActivity(new Intent(this, CategoriesActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            ReminderScheduler.cancelAllForUser(this, session.getUserId());
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
