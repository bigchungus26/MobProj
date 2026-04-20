package com.mobproj.habittracker.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.CategoryDao;
import com.mobproj.habittracker.model.Category;
import com.mobproj.habittracker.ui.adapter.CategoryAdapter;

import java.util.Arrays;
import java.util.List;

public class CategoriesActivity extends BaseActivity {

    private static final String[] COLOR_OPTIONS = {
            "#4CAF50", "#2196F3", "#9C27B0", "#F44336", "#FF9800", "#00BCD4", "#607D8B"
    };

    private RecyclerView recycler;
    private View emptyState;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_categories);
        emptyState = findViewById(R.id.empty_state);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        fab.setOnClickListener(v -> showEditDialog(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        List<Category> list = new CategoryDao(this).findAllForUser(session.getUserId());
        if (list.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        adapter = new CategoryAdapter(list, new CategoryAdapter.Listener() {
            @Override
            public void onEdit(Category category) {
                showEditDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                new AlertDialog.Builder(CategoriesActivity.this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(getString(R.string.confirm_delete_category, category.getName()))
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            new CategoryDao(CategoriesActivity.this).delete(category.getId());
                            loadCategories();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);
    }

    private void showEditDialog(@Nullable Category existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null, false);
        EditText nameInput = view.findViewById(R.id.input_name);
        Spinner typeSpinner = view.findViewById(R.id.spinner_type);
        Spinner colorSpinner = view.findViewById(R.id.spinner_color);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Arrays.asList(COLOR_OPTIONS));
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        if (existing != null) {
            nameInput.setText(existing.getName());
            typeSpinner.setSelection(Category.TYPE_WORKOUT.equals(existing.getType()) ? 1 : 0);
            for (int i = 0; i < COLOR_OPTIONS.length; i++) {
                if (COLOR_OPTIONS[i].equalsIgnoreCase(existing.getColor())) {
                    colorSpinner.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.title_new_category : R.string.title_edit_category)
                .setView(view)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, R.string.error_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Category cat = existing != null ? existing : new Category();
                    cat.setUserId(session.getUserId());
                    cat.setName(name);
                    cat.setType(typeSpinner.getSelectedItemPosition() == 1
                            ? Category.TYPE_WORKOUT : Category.TYPE_HABIT);
                    cat.setColor(COLOR_OPTIONS[colorSpinner.getSelectedItemPosition()]);

                    CategoryDao dao = new CategoryDao(this);
                    if (existing != null) dao.update(cat);
                    else dao.insert(cat);
                    loadCategories();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

}
