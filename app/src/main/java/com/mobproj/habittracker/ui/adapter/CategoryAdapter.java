package com.mobproj.habittracker.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {

    public interface Listener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private final List<Category> items;
    private final Listener listener;

    public CategoryAdapter(List<Category> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Category c = items.get(position);
        h.name.setText(c.getName());
        h.type.setText(c.getType());
        try {
            h.colorDot.setBackgroundColor(Color.parseColor(
                    c.getColor() != null ? c.getColor() : "#CCCCCC"));
        } catch (IllegalArgumentException e) {
            h.colorDot.setBackgroundColor(Color.GRAY);
        }
        h.itemView.setOnClickListener(v -> listener.onEdit(c));
        h.deleteBtn.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView type;
        final View colorDot;
        final ImageButton deleteBtn;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            type = itemView.findViewById(R.id.text_type);
            colorDot = itemView.findViewById(R.id.color_dot);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }
}
