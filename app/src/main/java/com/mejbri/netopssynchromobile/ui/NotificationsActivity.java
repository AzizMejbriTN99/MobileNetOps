package com.mejbri.netopssynchromobile.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.Notification;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;

import java.util.List;

import retrofit2.*;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recycler = findViewById(R.id.rvNotifications);

        adapter = new NotificationsAdapter();

        recycler.setLayoutManager(
                new LinearLayoutManager(this));

        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

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

                        if (!response.isSuccessful()
                                || response.body() == null)
                            return;

                        adapter.setData(response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<List<Notification>> call,
                            Throwable t) {
                    }
                });
    }

    private void markAllRead() {

        ApiClient.create(TechnicianApi.class)
                .markAllRead()
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t) {
                    }
                });
    }
}