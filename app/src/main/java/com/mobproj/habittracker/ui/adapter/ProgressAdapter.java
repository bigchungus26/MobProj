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

import java.util.ArrayList;
import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECORD = 1;

    private static class Row {
        final int type;
        final String header;
        final ProgressRecord record;

        Row(String header) {
            this.type = TYPE_HEADER;
            this.header = header;
            this.record = null;
        }

        Row(ProgressRecord record) {
            this.type = TYPE_RECORD;
            this.header = null;
            this.record = record;
        }
    }

    private final List<Row> rows;

    public ProgressAdapter(List<ProgressRecord> records) {
        this.rows = buildRows(records);
    }

    private static List<Row> buildRows(List<ProgressRecord> records) {
        List<Row> out = new ArrayList<>();
        String lastDate = null;
        for (ProgressRecord r : records) {
            if (!r.getDate().equals(lastDate)) {
                out.add(new Row(DateUtils.formatIsoForDisplay(r.getDate())));
                lastDate = r.getDate();
            }
            out.add(new Row(r));
        }
        return out;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(
                    R.layout.item_progress_header, parent, false));
        }
        return new RecordHolder(inflater.inflate(R.layout.item_progress, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.type == TYPE_HEADER) {
            ((HeaderHolder) holder).title.setText(row.header);
        } else {
            RecordHolder h = (RecordHolder) holder;
            ProgressRecord r = row.record;
            h.name.setText(r.getItemName() != null ? r.getItemName()
                    : h.itemView.getContext().getString(R.string.unknown_item));
            h.date.setText(DateUtils.formatIsoForDisplay(r.getDate()));
            h.type.setText(r.getItemType());
            h.status.setText(r.isCompleted()
                    ? R.string.status_completed : R.string.status_not_completed);
            int colorRes = r.isCompleted() ? R.color.status_completed : R.color.status_pending;
            int color = ContextCompat.getColor(h.itemView.getContext(), colorRes);
            h.status.setTextColor(color);
            h.indicator.setBackgroundColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_header);
        }
    }

    static class RecordHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView date;
        final TextView type;
        final TextView status;
        final View indicator;

        RecordHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            date = itemView.findViewById(R.id.text_date);
            type = itemView.findViewById(R.id.text_type);
            status = itemView.findViewById(R.id.text_status);
            indicator = itemView.findViewById(R.id.status_indicator);
        }
    }
}
