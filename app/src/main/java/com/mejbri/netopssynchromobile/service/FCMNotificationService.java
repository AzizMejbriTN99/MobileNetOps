package com.mejbri.netopssynchromobile.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mejbri.netopssynchromobile.MainActivity;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import com.mejbri.netopssynchromobile.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String TAG        = "FCMService";
    private static final String CHANNEL_ID = "netops_tasks";

    /**
     * Called by FCM whenever this device receives a new or refreshed token.
     * Register it with our backend immediately if the user is already logged in.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "FCM token refreshed");
        if (SessionManager.isLoggedIn(this)) {
            registerToken(this, token);
        }
    }

    /** Called for every incoming FCM message while the app is in foreground. */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = "NetOps Synchro";
        String body  = "";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null)
                title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null)
                body = message.getNotification().getBody();
        } else {
            title = message.getData().getOrDefault("title", title);
            body  = message.getData().getOrDefault("body", body);
        }

        showNotification(title, body);
    }

    // ── Local notification display ─────────────────────────────────────────────

    private void showNotification(String title, String body) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "NetOps Tasks", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Task assignments and updates");
        manager.createNotificationChannel(ch); // no-op if already exists

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        manager.notify((int) System.currentTimeMillis(),
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .build());
    }

    // ── Backend registration ───────────────────────────────────────────────────

    /**
     * Call this from anywhere (e.g. LoginActivity after successful login) to
     * register the current device token with the backend.
     */
    public static void registerToken(Context context, String token) {
        if (token == null || token.isBlank()) return;
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        ApiClient.create(TechnicianApi.class).registerFcmToken(body)
                .enqueue(new Callback<>() {
                    @Override public void onResponse(Call<Map<String, String>> c,
                                                     Response<Map<String, String>> r) {
                        Log.d(TAG, "FCM token sent to backend: " + r.isSuccessful());
                    }
                    @Override public void onFailure(Call<Map<String, String>> c, Throwable t) {
                        Log.w(TAG, "FCM token registration failed: " + t.getMessage());
                    }
                });
    }
}
