package com.mobproj.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.model.ProgressRecord;
import com.mobproj.habittracker.util.DateUtils;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.Holder> {

    private final List<ProgressRecord> items;

    public ProgressAdapter(List<ProgressRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progress, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ProgressRecord r = items.get(position);
        h.name.setText(r.getItemName() != null ? r.getItemName()
                : h.itemView.getContext().getString(R.string.unknown_item));
        h.date.setText(DateUtils.formatIsoForDisplay(r.getDate()));
        h.type.setText(r.getItemType());
        h.status.setText(r.isCompleted()
                ? R.string.status_completed : R.string.status_not_completed);
        int color = r.isCompleted() ? R.color.status_completed : R.color.status_pending;
        h.status.setTextColor(ContextCompat.getColor(h.itemView.getContext(), color));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView date;
        final TextView type;
        final TextView status;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            date = itemView.findViewById(R.id.text_date);
            type = itemView.findViewById(R.id.text_type);
            status = itemView.findViewById(R.id.text_status);
        }
    }
}
