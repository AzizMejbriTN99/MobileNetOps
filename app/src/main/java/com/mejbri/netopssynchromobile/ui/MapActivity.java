package com.mejbri.netopssynchromobile.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.Demande;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
        try {
            gMap.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {}

        loadDemandeMarkers();
    }

    private void loadDemandeMarkers() {
        ApiClient.create(TechnicianApi.class).getMyDemandes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<Demande>> call, Response<List<Demande>> r) {
                        if (!r.isSuccessful() || r.body() == null) return;
                        runOnUiThread(() -> {
                            for (Demande d : r.body()) {
                                if (d.latitude == null || d.longitude == null) continue;
                                gMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(d.latitude, d.longitude))
                                        .title(d.title)
                                        .snippet(d.clientName + " · " + d.status));
                            }
                            if (!r.body().isEmpty()) {
                                Demande first = r.body().get(0);
                                if (first.latitude != null)
                                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(first.latitude, first.longitude), 12f));
                            }
                        });
                    }
                    @Override public void onFailure(Call<List<Demande>> call, Throwable t) {}
                });
    }
}
