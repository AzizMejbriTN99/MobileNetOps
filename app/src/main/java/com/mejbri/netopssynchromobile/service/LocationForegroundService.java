package com.mejbri.netopssynchromobile.service;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationRequest;
import com.mejbri.netopssynchromobile.MainActivity;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.LocationUpdate;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIF_ID      = 1001;

    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000) // every 10s
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) sendLocation(loc.getLatitude(), loc.getLongitude());
            }
        };

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            stopSelf();
        }
    }

    private void sendLocation(double lat, double lng) {
        ApiClient.create(TechnicianApi.class)
                .updateLocation(new LocationUpdate(lat, lng))
                .enqueue(new Callback<>() {
                    @Override public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> r) {}
                    @Override public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                });
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NetOps — Location Active")
                .setContentText("Your location is being tracked for active tasks")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Location Tracking", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Tracks technician location for task management");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedClient != null && locationCallback != null)
            fusedClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

}
