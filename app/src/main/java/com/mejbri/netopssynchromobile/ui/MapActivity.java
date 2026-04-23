package com.mejbri.netopssynchromobile.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

    private static final String TAG = "MapActivity";
    private GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Maps renderer explicitly before setting content view.
        // This avoids the "preferredRenderer: null" issue seen in logs where
        // the renderer isn't chosen and tiles fail to load.
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST,
                result -> Log.d(TAG, "Maps renderer initialized: " + result));

        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;

        // Map type and basic settings
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setZoomGesturesEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);

        try {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.w(TAG, "Location permission not granted: " + e.getMessage());
        }

        loadDemandeMarkers();
    }

    private void loadDemandeMarkers() {
        ApiClient.create(TechnicianApi.class).getMyDemandes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<Demande>> call, Response<List<Demande>> r) {
                        if (!r.isSuccessful() || r.body() == null) return;
                        runOnUiThread(() -> {
                            List<Demande> demandes = r.body();
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            int pinned = 0;

                            for (Demande d : demandes) {
                                if (d.latitude == null || d.longitude == null) continue;
                                LatLng pos = new LatLng(d.latitude, d.longitude);

                                // Color marker by status
                                float hue = BitmapDescriptorFactory.HUE_AZURE;
                                if ("IN_PROGRESS".equals(d.status))   hue = BitmapDescriptorFactory.HUE_ORANGE;
                                else if ("RESOLVED".equals(d.status)) hue = BitmapDescriptorFactory.HUE_GREEN;
                                else if ("CLOSED".equals(d.status))   hue = BitmapDescriptorFactory.HUE_VIOLET;

                                gMap.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .title(d.title)
                                        .snippet(d.clientName + " · " + d.status.replace("_", " "))
                                        .icon(BitmapDescriptorFactory.defaultMarker(hue)));

                                boundsBuilder.include(pos);
                                pinned++;
                            }

                            if (pinned == 1) {
                                // Only one marker — just zoom to it
                                Demande first = demandes.get(0);
                                if (first.latitude != null)
                                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(first.latitude, first.longitude), 12f));
                            } else if (pinned > 1) {
                                // Multiple markers — fit all in view with padding
                                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                                        boundsBuilder.build(), 120));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Demande>> call, Throwable t) {
                        Log.e(TAG, "Failed to load demande markers: " + t.getMessage());
                    }
                });
    }
}
