package com.mobproj.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.UserDao;
import com.mobproj.habittracker.notify.ReminderScheduler;
import com.mobproj.habittracker.util.SessionManager;

public class SettingsActivity extends BaseActivity {

    private RadioGroup themeGroup;
    private MaterialSwitch remindersSwitch;
    private EditText dailyGoalInput;
    private TextView usernameText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        themeGroup = findViewById(R.id.group_theme);
        remindersSwitch = findViewById(R.id.switch_reminders);
        dailyGoalInput = findViewById(R.id.input_daily_goal);
        usernameText = findViewById(R.id.text_username);
        MaterialButton saveBtn = findViewById(R.id.btn_save);
        MaterialButton logoutBtn = findViewById(R.id.btn_logout);
        MaterialButton changePwBtn = findViewById(R.id.btn_change_password);

        loadCurrentSettings();

        saveBtn.setOnClickListener(v -> saveSettings());
        logoutBtn.setOnClickListener(v -> logout());
        changePwBtn.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_change_password, null, false);
        EditText currentInput = view.findViewById(R.id.input_current);
        EditText newInput = view.findViewById(R.id.input_new);
        EditText confirmInput = view.findViewById(R.id.input_confirm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_change_password)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String current = currentInput.getText().toString();
                    String newPw = newInput.getText().toString();
                    String confirm = confirmInput.getText().toString();

                    if (TextUtils.isEmpty(current) || TextUtils.isEmpty(newPw)) {
                        Toast.makeText(this, R.string.error_fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPw.length() < 6) {
                        Toast.makeText(this, R.string.error_password_short,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPw.equals(confirm)) {
                        Toast.makeText(this, R.string.error_password_mismatch,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean ok = new UserDao(this)
                            .changePassword(session.getUserId(), current, newPw);
                    if (!ok) {
                        Toast.makeText(this, R.string.error_current_password_wrong,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, R.string.msg_password_changed,
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void loadCurrentSettings() {
        usernameText.setText(getString(R.string.signed_in_as, session.getUsername()));

        switch (session.getTheme()) {
            case SessionManager.THEME_LIGHT:
                themeGroup.check(R.id.radio_theme_light);
                break;
            case SessionManager.THEME_DARK:
                themeGroup.check(R.id.radio_theme_dark);
                break;
            default:
                themeGroup.check(R.id.radio_theme_system);
        }

        remindersSwitch.setChecked(session.areRemindersEnabled());
        dailyGoalInput.setText(String.valueOf(session.getDailyGoal()));
    }

    private void saveSettings() {
        int themeId = themeGroup.getCheckedRadioButtonId();
        String theme;
        if (themeId == R.id.radio_theme_light) theme = SessionManager.THEME_LIGHT;
        else if (themeId == R.id.radio_theme_dark) theme = SessionManager.THEME_DARK;
        else theme = SessionManager.THEME_SYSTEM;
        session.setTheme(theme);

        session.setRemindersEnabled(remindersSwitch.isChecked());
        ReminderScheduler.rescheduleAllForCurrentUser(this);

        int goal = session.getDailyGoal();
        try {
            goal = Integer.parseInt(dailyGoalInput.getText().toString().trim());
            if (goal < 1) goal = 1;
        } catch (NumberFormatException ignored) {}
        session.setDailyGoal(goal);

        Toast.makeText(this, R.string.msg_settings_saved, Toast.LENGTH_SHORT).show();
        applyTheme(theme);
        recreate();
    }

    private void logout() {
        ReminderScheduler.cancelAllForUser(this, session.getUserId());
        session.signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
