package com.mejbri.netopssynchromobile.ui;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.TechnicianResource;
import java.util.*;

public class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.VH> {

    interface OnDelete { void onDelete(long id); }

    private List<TechnicianResource> data = new ArrayList<>();
    private final OnDelete listener;

    public ResourcesAdapter(OnDelete listener) { this.listener = listener; }
    public void setData(List<TechnicianResource> list) { data = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resource, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TechnicianResource r = data.get(pos);
        h.tvName.setText(r.resourceName);
        h.tvQty.setText(r.quantity + " " + (r.unit != null ? r.unit : "units"));
        h.tvNote.setText(r.notes != null ? r.notes : "");
        h.tvNote.setVisibility(r.notes != null && !r.notes.isEmpty() ? View.VISIBLE : View.GONE);
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r.id));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvNote;
        ImageButton btnDelete;
        VH(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvName);
            tvQty     = v.findViewById(R.id.tvQty);
            tvNote    = v.findViewById(R.id.tvNote);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
