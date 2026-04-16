package com.mejbri.netopssynchromobile.ui;


import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.DemandeAction;
import java.util.*;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.VH> {

    private List<DemandeAction> data = new ArrayList<>();

    public void setData(List<DemandeAction> list) { data = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DemandeAction a = data.get(pos);
        h.tvStatus.setText(a.status.replace("_", " "));
        h.tvNote.setText(a.note != null && !a.note.isEmpty() ? a.note : "");
        h.tvMeta.setText(a.performedBy + " · " + (a.performedAt != null ? a.performedAt.substring(11, 16) : ""));
        h.vLine.setVisibility(pos < data.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStatus, tvNote, tvMeta;
        View vLine;
        VH(View v) {
            super(v);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvNote   = v.findViewById(R.id.tvNote);
            tvMeta   = v.findViewById(R.id.tvMeta);
            vLine    = v.findViewById(R.id.vLine);
        }
    }
}
