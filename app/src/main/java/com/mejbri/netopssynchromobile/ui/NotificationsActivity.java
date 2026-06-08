package com.mejbri.netopssynchromobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.Notification;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import java.util.List;
import java.util.Map;
import retrofit2.*;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private NotificationsAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recycler = findViewById(R.id.rvNotifications);
        tvEmpty  = findViewById(R.id.tvEmpty);

        adapter = new NotificationsAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnMarkAll).setOnClickListener(v -> markAllRead());

        loadNotifications();
        markAllRead();
    }

    private void loadNotifications() {
        ApiClient.create(TechnicianApi.class)
                .getNotifications()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<Notification>> call,
                            Response<List<Notification>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        List<Notification> data = response.body();
                        runOnUiThread(() -> {
                            adapter.setData(data);
                            tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {}
                });
    }

    private void markAllRead() {
        ApiClient.create(TechnicianApi.class)
                .markAllRead()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<Map<String, String>> call,
                            Response<Map<String, String>> response) {}

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                });
    }
}