package com.mobproj.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.mobproj.habittracker.R;
import com.mobproj.habittracker.model.Habit;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.Holder> {

    public interface Listener {
        boolean isCompletedToday(Habit habit);
        int getStreak(Habit habit);
        void onToggle(Habit habit, boolean checked);
        void onEdit(Habit habit);
        void onDelete(Habit habit);
    }

    private final List<Habit> items;
    private final Listener listener;

    public HabitAdapter(List<Habit> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Habit habit = items.get(position);
        h.name.setText(habit.getName());
        String meta = habit.getFrequency();
        if (habit.getCategoryName() != null) {
            meta = habit.getCategoryName() + " • " + meta;
        }
        if (habit.getReminderTime() != null) {
            meta += " • " + habit.getReminderTime();
        }
        h.meta.setText(meta);
        if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
            h.description.setVisibility(View.VISIBLE);
            h.description.setText(habit.getDescription());
        } else {
            h.description.setVisibility(View.GONE);
        }

        int streak = listener.getStreak(habit);
        if (streak > 0) {
            h.streakChip.setVisibility(View.VISIBLE);
            h.streakText.setText(h.itemView.getContext().getString(R.string.streak_chip, streak));
        } else {
            h.streakChip.setVisibility(View.GONE);
        }

        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(listener.isCompletedToday(habit));
        h.checkBox.setOnCheckedChangeListener((v, checked) ->
                listener.onToggle(habit, checked));

        h.itemView.setOnClickListener(v -> listener.onEdit(habit));
        h.deleteBtn.setOnClickListener(v -> listener.onDelete(habit));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkBox;
        final TextView name;
        final TextView meta;
        final TextView description;
        final View streakChip;
        final TextView streakText;
        final ImageButton deleteBtn;

        Holder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_done);
            name = itemView.findViewById(R.id.text_name);
            meta = itemView.findViewById(R.id.text_meta);
            description = itemView.findViewById(R.id.text_description);
            streakChip = itemView.findViewById(R.id.streak_chip);
            streakText = itemView.findViewById(R.id.text_streak);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }
}
