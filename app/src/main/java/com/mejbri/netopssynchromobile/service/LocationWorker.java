package com.mejbri.netopssynchromobile.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Tasks;
import com.mejbri.netopssynchromobile.model.LocationUpdate;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import com.mejbri.netopssynchromobile.util.SessionManager;

public class LocationWorker extends Worker {

    public LocationWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }


    @NonNull
    @Override
    public Result doWork() {
        if (!SessionManager.isLoggedIn(getApplicationContext())) return Result.success();
        try {
            FusedLocationProviderClient client =
                    LocationServices.getFusedLocationProviderClient(getApplicationContext());
            @SuppressLint("MissingPermission") Location loc = Tasks.await(client.getLastLocation());
            if (loc != null) {
                ApiClient.create(TechnicianApi.class)
                        .updateLocation(new LocationUpdate(loc.getLatitude(), loc.getLongitude()))
                        .execute();
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
