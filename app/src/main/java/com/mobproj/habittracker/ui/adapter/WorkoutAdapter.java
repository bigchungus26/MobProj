package com.mobproj.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.model.Workout;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.Holder> {

    public interface Listener {
        boolean isCompletedToday(Workout workout);
        void onToggle(Workout workout, boolean checked);
        void onEdit(Workout workout);
        void onDelete(Workout workout);
    }

    private final List<Workout> items;
    private final Listener listener;

    public WorkoutAdapter(List<Workout> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Workout workout = items.get(position);
        h.name.setText(workout.getName());
        StringBuilder meta = new StringBuilder();
        if (workout.getCategoryName() != null) {
            meta.append(workout.getCategoryName()).append(" • ");
        }
        meta.append(workout.getDurationMinutes()).append(" min • ")
                .append(workout.getIntensity());
        h.meta.setText(meta.toString());

        if (workout.getDescription() != null && !workout.getDescription().isEmpty()) {
            h.description.setVisibility(View.VISIBLE);
            h.description.setText(workout.getDescription());
        } else {
            h.description.setVisibility(View.GONE);
        }

        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(listener.isCompletedToday(workout));
        h.checkBox.setOnCheckedChangeListener((v, checked) ->
                listener.onToggle(workout, checked));

        h.itemView.setOnClickListener(v -> listener.onEdit(workout));
        h.deleteBtn.setOnClickListener(v -> listener.onDelete(workout));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView name;
        final TextView meta;
        final TextView description;
        final ImageButton deleteBtn;

        Holder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_done);
            name = itemView.findViewById(R.id.text_name);
            meta = itemView.findViewById(R.id.text_meta);
            description = itemView.findViewById(R.id.text_description);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }
}
