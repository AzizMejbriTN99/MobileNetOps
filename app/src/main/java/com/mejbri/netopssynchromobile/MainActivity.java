package com.mejbri.netopssynchromobile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.*;
import com.mejbri.netopssynchromobile.model.Demande;
import com.mejbri.netopssynchromobile.network.*;
import com.mejbri.netopssynchromobile.service.*;
import com.mejbri.netopssynchromobile.ui.DemandeDetailActivity;
import com.mejbri.netopssynchromobile.ui.DemandesAdapter;
import com.mejbri.netopssynchromobile.ui.LoginActivity;
import com.mejbri.netopssynchromobile.ui.MapActivity;
import com.mejbri.netopssynchromobile.ui.ResourcesActivity;
import com.mejbri.netopssynchromobile.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_LOCATION = 100;
    private RecyclerView     rvDemandes;
    private SwipeRefreshLayout swipeRefresh;
    private TextView         tvEmpty, tvWelcome;
    private DemandesAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome    = findViewById(R.id.tvWelcome);
        rvDemandes   = findViewById(R.id.rvDemandes);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty      = findViewById(R.id.tvEmpty);

        tvWelcome.setText("Welcome, " + SessionManager.getUsername(this));

        adapter = new DemandesAdapter(demande -> {
            Intent intent = new Intent(this, DemandeDetailActivity.class);
            intent.putExtra("demandeId", demande.id);
            startActivity(intent);
        });

        rvDemandes.setLayoutManager(new LinearLayoutManager(this));
        rvDemandes.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadDemandes);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        // toolbar logout + map buttons
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnMap).setOnClickListener(v ->
                startActivity(new Intent(this, MapActivity.class)));
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
    }

    private void loadDemandes() {
        swipeRefresh.setRefreshing(true);
        ApiClient.create(TechnicianApi.class).getMyDemandes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<Demande>> call, Response<List<Demande>> r) {
                        runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                            if (r.isSuccessful() && r.body() != null) {
                                List<Demande> list = r.body();
                                adapter.setData(list);
                                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Demande>> call, Throwable t) {
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });
    }

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
        Intent intent = new Intent(this, LocationForegroundService.class);
        startForegroundService(intent);
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

    private void logout() {
        stopService(new Intent(this, LocationForegroundService.class));
        WorkManager.getInstance(this).cancelAllWork();
        SessionManager.clear(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
