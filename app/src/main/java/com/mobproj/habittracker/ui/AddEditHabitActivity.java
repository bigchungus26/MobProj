package com.mobproj.habittracker.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.CategoryDao;
import com.mobproj.habittracker.data.HabitDao;
import com.mobproj.habittracker.model.Category;
import com.mobproj.habittracker.model.Habit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddEditHabitActivity extends BaseActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private EditText nameInput;
    private EditText descriptionInput;
    private Spinner categorySpinner;
    private RadioGroup frequencyGroup;
    private TextView reminderText;
    private Button reminderBtn;
    private Button saveBtn;

    private List<Category> categories;
    private long habitId = -1L;
    private Habit existingHabit;
    private String reminderTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameInput = findViewById(R.id.input_name);
        descriptionInput = findViewById(R.id.input_description);
        categorySpinner = findViewById(R.id.spinner_category);
        frequencyGroup = findViewById(R.id.group_frequency);
        reminderText = findViewById(R.id.text_reminder);
        reminderBtn = findViewById(R.id.btn_pick_time);
        saveBtn = findViewById(R.id.btn_save);

        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, -1L);
        loadCategories();

        if (habitId != -1L) {
            setTitle(R.string.title_edit_habit);
            existingHabit = new HabitDao(this).findById(habitId);
            populateExisting();
        } else {
            setTitle(R.string.title_new_habit);
        }

        reminderBtn.setOnClickListener(v -> pickTime());
        saveBtn.setOnClickListener(v -> save());
    }

    private void loadCategories() {
        categories = new ArrayList<>();
        categories.add(noneCategory());
        categories.addAll(new CategoryDao(this).findByType(
                session.getUserId(), Category.TYPE_HABIT));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private Category noneCategory() {
        Category none = new Category();
        none.setId(-1L);
        none.setName(getString(R.string.none));
        return none;
    }

    private void populateExisting() {
        if (existingHabit == null) return;
        nameInput.setText(existingHabit.getName());
        descriptionInput.setText(existingHabit.getDescription());
        if (Habit.FREQ_WEEKLY.equals(existingHabit.getFrequency())) {
            frequencyGroup.check(R.id.radio_weekly);
        } else {
            frequencyGroup.check(R.id.radio_daily);
        }
        if (existingHabit.getCategoryId() != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == existingHabit.getCategoryId()) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
        reminderTime = existingHabit.getReminderTime();
        updateReminderDisplay();
    }

    private void pickTime() {
        int hour = 9, minute = 0;
        if (reminderTime != null && reminderTime.contains(":")) {
            try {
                String[] parts = reminderTime.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        new TimePickerDialog(this, (view, h, m) -> {
            reminderTime = String.format(Locale.US, "%02d:%02d", h, m);
            updateReminderDisplay();
        }, hour, minute, true).show();
    }

    private void updateReminderDisplay() {
        reminderText.setText(reminderTime != null
                ? getString(R.string.reminder_at, reminderTime)
                : getString(R.string.no_reminder));
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Habit habit = existingHabit != null ? existingHabit : new Habit();
        habit.setUserId(session.getUserId());
        habit.setName(name);
        habit.setDescription(descriptionInput.getText().toString().trim());
        Category selected = (Category) categorySpinner.getSelectedItem();
        habit.setCategoryId(selected != null && selected.getId() > 0
                ? selected.getId() : null);
        habit.setFrequency(frequencyGroup.getCheckedRadioButtonId() == R.id.radio_weekly
                ? Habit.FREQ_WEEKLY : Habit.FREQ_DAILY);
        habit.setReminderTime(reminderTime);

        HabitDao dao = new HabitDao(this);
        if (existingHabit != null) {
            dao.update(habit);
        } else {
            dao.insert(habit);
        }
        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
