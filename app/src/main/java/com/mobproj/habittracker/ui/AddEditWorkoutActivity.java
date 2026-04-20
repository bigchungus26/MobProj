package com.mobproj.habittracker.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.CategoryDao;
import com.mobproj.habittracker.data.WorkoutDao;
import com.mobproj.habittracker.model.Category;
import com.mobproj.habittracker.model.Workout;

import java.util.ArrayList;
import java.util.List;

public class AddEditWorkoutActivity extends BaseActivity {

    public static final String EXTRA_WORKOUT_ID = "workout_id";

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText durationInput;
    private Spinner categorySpinner;
    private MaterialButtonToggleGroup intensityGroup;

    private List<Category> categories;
    private Workout existingWorkout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_workout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameInput = findViewById(R.id.input_name);
        descriptionInput = findViewById(R.id.input_description);
        durationInput = findViewById(R.id.input_duration);
        categorySpinner = findViewById(R.id.spinner_category);
        intensityGroup = findViewById(R.id.group_intensity);
        MaterialButton saveBtn = findViewById(R.id.btn_save);

        long workoutId = getIntent().getLongExtra(EXTRA_WORKOUT_ID, -1L);
        loadCategories();

        if (workoutId != -1L) {
            toolbar.setTitle(R.string.title_edit_workout);
            existingWorkout = new WorkoutDao(this).findById(workoutId);
            populateExisting();
        } else {
            toolbar.setTitle(R.string.title_new_workout);
            intensityGroup.check(R.id.btn_medium);
        }

        saveBtn.setOnClickListener(v -> save());
    }

    private void loadCategories() {
        categories = new ArrayList<>();
        Category none = new Category();
        none.setId(-1L);
        none.setName(getString(R.string.none));
        categories.add(none);
        categories.addAll(new CategoryDao(this).findByType(
                session.getUserId(), Category.TYPE_WORKOUT));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void populateExisting() {
        if (existingWorkout == null) return;
        nameInput.setText(existingWorkout.getName());
        descriptionInput.setText(existingWorkout.getDescription());
        durationInput.setText(String.valueOf(existingWorkout.getDurationMinutes()));
        String intensity = existingWorkout.getIntensity();
        if (Workout.INTENSITY_LOW.equals(intensity)) {
            intensityGroup.check(R.id.btn_low);
        } else if (Workout.INTENSITY_HIGH.equals(intensity)) {
            intensityGroup.check(R.id.btn_high);
        } else {
            intensityGroup.check(R.id.btn_medium);
        }
        if (existingWorkout.getCategoryId() != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == existingWorkout.getCategoryId()) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = 0;
        String durationText = durationInput.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            try {
                duration = Integer.parseInt(durationText);
            } catch (NumberFormatException ignored) {}
        }

        Workout workout = existingWorkout != null ? existingWorkout : new Workout();
        workout.setUserId(session.getUserId());
        workout.setName(name);
        workout.setDescription(descriptionInput.getText().toString().trim());
        workout.setDurationMinutes(duration);
        Category selected = (Category) categorySpinner.getSelectedItem();
        workout.setCategoryId(selected != null && selected.getId() > 0
                ? selected.getId() : null);

        int intensityId = intensityGroup.getCheckedButtonId();
        if (intensityId == R.id.btn_low) workout.setIntensity(Workout.INTENSITY_LOW);
        else if (intensityId == R.id.btn_high) workout.setIntensity(Workout.INTENSITY_HIGH);
        else workout.setIntensity(Workout.INTENSITY_MEDIUM);

        WorkoutDao dao = new WorkoutDao(this);
        if (existingWorkout != null) {
            dao.update(workout);
        } else {
            dao.insert(workout);
        }
        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
