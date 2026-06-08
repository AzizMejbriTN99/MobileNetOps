package com.mejbri.netopssynchromobile.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter
        extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final List<Notification> items = new ArrayList<>();

    public void setData(List<Notification> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final View      vUnreadDot;
        private final TextView  tvMessage;
        private final TextView  tvTime;

        VH(View v) {
            super(v);
            vUnreadDot = v.findViewById(R.id.vUnreadDot);
            tvMessage  = v.findViewById(R.id.tvMessage);
            tvTime     = v.findViewById(R.id.tvTime);
        }

        void bind(Notification n) {
            tvMessage.setText(n.message != null ? n.message : "");

            // Show the blue dot only for unread notifications
            vUnreadDot.setVisibility(n.read ? View.INVISIBLE : View.VISIBLE);

            // Make unread messages bold
            tvMessage.setTypeface(null,
                    n.read
                            ? android.graphics.Typeface.NORMAL
                            : android.graphics.Typeface.BOLD);

            // Format timestamp: show only the date+time portion if ISO string
            if (n.createdAt != null && !n.createdAt.isEmpty()) {
                // "2025-06-01T14:32:00" → "2025-06-01  14:32"
                String display = n.createdAt.replace("T", "  ");
                if (display.length() > 16) display = display.substring(0, 16);
                tvTime.setText(display);
            } else {
                tvTime.setText("");
            }
        }
    }
}