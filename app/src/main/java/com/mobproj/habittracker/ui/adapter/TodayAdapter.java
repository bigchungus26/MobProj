package com.mobproj.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.mobproj.habittracker.R;

import java.util.List;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.Holder> {

    public static class Item {
        public final long id;
        public final String type;
        public final String title;
        public final String subtitle;
        public final boolean completed;

        public Item(long id, String type, String title, String subtitle, boolean completed) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.completed = completed;
        }
    }

    public interface OnToggle {
        void onToggle(Item item, boolean checked);
    }

    private final List<Item> items;
    private final OnToggle listener;

    public TodayAdapter(List<Item> items, OnToggle listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Item item = items.get(position);
        h.title.setText(item.title);
        h.subtitle.setText(item.subtitle);
        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(item.completed);
        h.checkBox.setOnCheckedChangeListener((v, checked) ->
                listener.onToggle(item, checked));
        h.itemView.setOnClickListener(v ->
                h.checkBox.setChecked(!h.checkBox.isChecked()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkBox;
        final TextView title;
        final TextView subtitle;

        Holder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_done);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }
    }
}
