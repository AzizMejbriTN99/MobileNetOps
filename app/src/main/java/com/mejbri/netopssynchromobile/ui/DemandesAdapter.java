package com.mejbri.netopssynchromobile.ui;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.Demande;
import java.util.*;

public class DemandesAdapter extends RecyclerView.Adapter<DemandesAdapter.VH> {

    public interface OnItemClick { void onClick(Demande d); }

    private List<Demande> data = new ArrayList<>();
    private final OnItemClick listener;

    public DemandesAdapter(OnItemClick listener) { this.listener = listener; }

    public void setData(List<Demande> list) {
        data = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_demande, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Demande d = data.get(pos);
        h.tvTitle.setText(d.title);
        h.tvClient.setText(d.clientName);
        h.tvLocation.setText(d.clientLocation != null ? d.clientLocation : "");
        h.tvStatus.setText(d.status.replace("_", " "));
        h.tvPriority.setText(d.priority);

        int statusColor;
        switch (d.status) {
            case "NEW":
                statusColor = Color.parseColor("#005fa3");
                break;
            case "IN_PROGRESS":
                statusColor = Color.parseColor("#b45309");
                break;
            case "RESOLVED":
                statusColor = Color.parseColor("#16a34a");
                break;
            default:
                statusColor = Color.parseColor("#999999");
                break;
        }
        h.tvStatus.setTextColor(statusColor);

        int priorityColor;
        switch (d.priority) {
            case "CRITICAL":
                priorityColor = Color.parseColor("#c0392b");
                break;
            case "HIGH":
                priorityColor = Color.parseColor("#b45309");
                break;
            case "MEDIUM":
                priorityColor = Color.parseColor("#005fa3");
                break;
            default:
                priorityColor = Color.parseColor("#16a34a");
                break;
        }
        h.tvPriority.setTextColor(priorityColor);

        h.itemView.setOnClickListener(v -> listener.onClick(d));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvClient, tvLocation, tvStatus, tvPriority;
        VH(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvClient   = v.findViewById(R.id.tvClient);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvStatus   = v.findViewById(R.id.tvStatus);
            tvPriority = v.findViewById(R.id.tvPriority);
        }
    }

    @Override public int getItemCount() { return data.size(); }

}
