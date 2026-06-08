package com.mejbri.netopssynchromobile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.*;
import com.mejbri.netopssynchromobile.model.Demande;
import com.mejbri.netopssynchromobile.network.*;
import com.mejbri.netopssynchromobile.service.*;
import com.mejbri.netopssynchromobile.ui.DemandeDetailActivity;
import com.mejbri.netopssynchromobile.ui.DemandesAdapter;
import com.mejbri.netopssynchromobile.ui.LoginActivity;
import com.mejbri.netopssynchromobile.ui.NotificationsActivity;
import com.mejbri.netopssynchromobile.ui.ProfileActivity;
import com.mejbri.netopssynchromobile.ui.ResourcesActivity;
import com.mejbri.netopssynchromobile.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_LOCATION = 100;

    // Active task views
    private View cardActiveTask;
    private View layoutNoActive;
    private TextView tvDisplayName, tvActiveTitle, tvActivePriority, tvActiveStatus,
                     tvActiveClient, tvActiveLocation;
    private Button btnOpenActive;
    private TextView tvAvatarInitials;
    private TextView tvNotifBadge;

    // History views
    private RecyclerView rvHistory;
    private TextView tvHistoryEmpty, tabResolved, tabClosed;
    private EditText etSearch;
    private DemandesAdapter historyAdapter;

    // Data
    private Demande activeTask;
    private List<Demande> allHistory = new ArrayList<>(); // RESOLVED + CLOSED
    private String activeTab = "RESOLVED"; // RESOLVED | CLOSED


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplayName = findViewById(R.id.tvDisplayName);
        cardActiveTask  = findViewById(R.id.cardActiveTask);
        layoutNoActive  = findViewById(R.id.layoutNoActive);
        tvActiveTitle   = findViewById(R.id.tvActiveTitle);
        tvActivePriority= findViewById(R.id.tvActivePriority);
        tvActiveStatus  = findViewById(R.id.tvActiveStatus);
        tvActiveClient  = findViewById(R.id.tvActiveClient);
        tvActiveLocation= findViewById(R.id.tvActiveLocation);
        btnOpenActive   = findViewById(R.id.btnOpenActive);
        rvHistory       = findViewById(R.id.rvHistory);
        tvHistoryEmpty  = findViewById(R.id.tvHistoryEmpty);
        tabResolved     = findViewById(R.id.tabResolved);
        tabClosed       = findViewById(R.id.tabClosed);
        etSearch        = findViewById(R.id.etSearch);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);


        tvDisplayName.setText("Welcome, " + SessionManager.getDisplayName(this));
        tvAvatarInitials.setText(SessionManager.getInitials(this));

        historyAdapter = new DemandesAdapter(demande -> openDetail(demande.id));
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);

        findViewById(R.id.btnProfile)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(this, ProfileActivity.class)
                        ));

        findViewById(R.id.btnNotifications)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(this, NotificationsActivity.class)
                        ));

        btnOpenActive.setOnClickListener(v -> {
            if (activeTask != null) openDetail(activeTask.id);
        });

        tabResolved.setOnClickListener(v -> setTab("RESOLVED"));
        tabClosed.setOnClickListener(v   -> setTab("CLOSED"));

        // Search bar — filter history list as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyHistoryFilter();
            }
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnResources).setOnClickListener(v ->
                startActivity(new Intent(this, ResourcesActivity.class)));

        requestLocationPermission();
        loadDemandes();
        scheduleBackgroundLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDemandes();
        loadNotificationCount();
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadDemandes() {
        ApiClient.create(TechnicianApi.class).getMyDemandes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<Demande>> call, Response<List<Demande>> r) {
                        if (!r.isSuccessful() || r.body() == null) return;
                        runOnUiThread(() -> {
                            activeTask = null;
                            allHistory.clear();
                            for (Demande d : r.body()) {
                                if ("NEW".equals(d.status) || "IN_PROGRESS".equals(d.status)) {
                                    // Take the most recent active task if somehow there's more than one
                                    if (activeTask == null) activeTask = d;
                                } else if ("RESOLVED".equals(d.status) || "CLOSED".equals(d.status)) {
                                    allHistory.add(d);
                                }
                            }
                            populateActiveTask();
                            applyHistoryFilter();
                        });
                    }
                    @Override
                    public void onFailure(Call<List<Demande>> call, Throwable t) {}
                });
    }

    // ── Active task card ──────────────────────────────────────────────────────

    private void populateActiveTask() {
        if (activeTask != null) {
            cardActiveTask.setVisibility(View.VISIBLE);
            layoutNoActive.setVisibility(View.GONE);
            tvActiveTitle.setText(activeTask.title);
            tvActiveClient.setText(activeTask.clientName);
            tvActiveLocation.setText(
                    activeTask.clientLocation != null ? activeTask.clientLocation : "—");
            tvActiveStatus.setText(activeTask.status.replace("_", " "));

            // Priority color
            String p = activeTask.priority != null ? activeTask.priority : "";
            int pc = p.equals("CRITICAL") ? Color.parseColor("#C0392B")
                   : p.equals("HIGH")     ? Color.parseColor("#B45309")
                   : p.equals("MEDIUM")   ? Color.parseColor("#005FA3")
                   :                        Color.parseColor("#16A34A");
            tvActivePriority.setText(p);
            tvActivePriority.setTextColor(pc);

            // Status color
            tvActiveStatus.setTextColor("IN_PROGRESS".equals(activeTask.status)
                    ? Color.parseColor("#B45309")
                    : Color.parseColor("#005FA3"));
        } else {
            cardActiveTask.setVisibility(View.GONE);
            layoutNoActive.setVisibility(View.VISIBLE);
        }
    }

    private void loadNotificationCount() {
        ApiClient.create(TechnicianApi.class)
                .getUnreadCount()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Map<String, Long>> call,
                                           Response<Map<String, Long>> response) {

                        if (!response.isSuccessful() || response.body() == null)
                            return;

                        Long count = response.body().get("count");

                        runOnUiThread(() -> {
                            if (count != null && count > 0) {
                                tvNotifBadge.setVisibility(View.VISIBLE);
                                tvNotifBadge.setText(String.valueOf(count));
                            } else {
                                tvNotifBadge.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                    }
                });
    }

    // ── History ───────────────────────────────────────────────────────────────

    private void setTab(String tab) {
        activeTab = tab;
        // Update tab chip visuals
        if ("RESOLVED".equals(tab)) {
            tabResolved.setBackgroundResource(R.drawable.btn_primary);
            tabResolved.setTextColor(Color.WHITE);
            tabClosed.setBackgroundResource(R.drawable.filter_chip_inactive);
            tabClosed.setTextColor(getColor(R.color.primary));
        } else {
            tabClosed.setBackgroundResource(R.drawable.btn_primary);
            tabClosed.setTextColor(Color.WHITE);
            tabResolved.setBackgroundResource(R.drawable.filter_chip_inactive);
            tabResolved.setTextColor(getColor(R.color.primary));
        }
        etSearch.setText(""); // clear search when switching tabs
        applyHistoryFilter();
    }

    private void applyHistoryFilter() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        List<Demande> filtered = new ArrayList<>();
        for (Demande d : allHistory) {
            if (!activeTab.equals(d.status)) continue;
            if (!query.isEmpty()) {
                boolean matches = (d.title != null && d.title.toLowerCase().contains(query))
                        || (d.clientName != null && d.clientName.toLowerCase().contains(query))
                        || (d.clientLocation != null && d.clientLocation.toLowerCase().contains(query));
                if (!matches) continue;
            }
            filtered.add(d);
        }
        historyAdapter.setData(filtered);
        tvHistoryEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openDetail(long id) {
        Intent intent = new Intent(this, DemandeDetailActivity.class);
        intent.putExtra("demandeId", id);
        startActivity(intent);
    }

    // ── Location ──────────────────────────────────────────────────────────────

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERM_LOCATION);
        } else {
            startLocationService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERM_LOCATION && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        }
    }

    private void startLocationService() {
        startForegroundService(new Intent(this, LocationForegroundService.class));
    }

    private void scheduleBackgroundLocation() {
        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(
                LocationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "location_bg", ExistingPeriodicWorkPolicy.KEEP, work);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void logout() {
        stopService(new Intent(this, LocationForegroundService.class));
        WorkManager.getInstance(this).cancelAllWork();
        SessionManager.clear(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
