package com.mejbri.netopssynchromobile.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.*;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.*;
import com.mejbri.netopssynchromobile.network.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.util.*;

public class DemandeDetailActivity extends AppCompatActivity {

    private static final int PERM_CAMERA = 201;

    private long demandeId;
    private Demande demande;
    private Uri photoUri;

    private TextView tvTitle, tvClient, tvLocation, tvContact, tvStatus, tvPriority, tvDescription;
    private Button   btnStatus, btnAction, btnPhoto, btnNavigate;
    private RecyclerView rvTimeline;
    private TimelineAdapter timelineAdapter;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && photoUri != null) {
                    uploadPhoto(photoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demande_detail);

        demandeId = getIntent().getLongExtra("demandeId", -1);
        if (demandeId == -1) { finish(); return; }

        bindViews();
        loadDemande();
        loadTimeline();
    }

    private void bindViews() {
        tvTitle       = findViewById(R.id.tvTitle);
        tvClient      = findViewById(R.id.tvClient);
        tvLocation    = findViewById(R.id.tvLocation);
        tvContact     = findViewById(R.id.tvContact);
        tvStatus      = findViewById(R.id.tvStatus);
        tvPriority    = findViewById(R.id.tvPriority);
        tvDescription = findViewById(R.id.tvDescription);
        btnStatus     = findViewById(R.id.btnStatus);
        btnAction     = findViewById(R.id.btnAction);
        btnPhoto      = findViewById(R.id.btnPhoto);
        btnNavigate   = findViewById(R.id.btnNavigate);
        rvTimeline    = findViewById(R.id.rvTimeline);

        timelineAdapter = new TimelineAdapter();
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);

        btnStatus.setOnClickListener(v -> showStatusDialog());
        btnAction.setOnClickListener(v -> showActionDialog());
        btnPhoto.setOnClickListener(v -> requestCameraAndTakePhoto());
        btnNavigate.setOnClickListener(v -> openNavigation());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void requestCameraAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.CAMERA }, PERM_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDemande() {
        ApiClient.create(TechnicianApi.class).getDemande(demandeId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Demande> call, Response<Demande> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            demande = r.body();
                            runOnUiThread(() -> populate());
                        }
                    }
                    @Override public void onFailure(Call<Demande> call, Throwable t) {}
                });
    }

    private void populate() {
        tvTitle.setText(demande.title);
        tvClient.setText(demande.clientName);
        tvLocation.setText(demande.clientLocation != null ? demande.clientLocation : "—");
        tvContact.setText(demande.clientContact != null ? demande.clientContact : "—");
        tvStatus.setText(demande.status.replace("_", " "));
        tvPriority.setText(demande.priority);
        tvDescription.setText(demande.description != null ? demande.description : "");
        btnNavigate.setVisibility(
                demande.latitude != null && demande.longitude != null ? View.VISIBLE : View.GONE);

        boolean isClosed = "CLOSED".equals(demande.status);
        btnStatus.setEnabled(!isClosed);
        btnAction.setEnabled(!isClosed);
        btnPhoto.setEnabled(!isClosed);
        btnStatus.setAlpha(isClosed ? 0.4f : 1f);
        btnAction.setAlpha(isClosed ? 0.4f : 1f);
        btnPhoto.setAlpha(isClosed ? 0.4f : 1f);
        if (isClosed) {
            btnStatus.setText("Task Closed");
            btnAction.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.GONE);
        }
    }

    private void loadTimeline() {
        ApiClient.create(TechnicianApi.class).getTimeline(demandeId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<DemandeAction>> call,
                                           Response<List<DemandeAction>> r) {
                        if (r.isSuccessful() && r.body() != null)
                            runOnUiThread(() -> timelineAdapter.setData(r.body()));
                    }
                    @Override public void onFailure(Call<List<DemandeAction>> call, Throwable t) {}
                });
    }

    private void showStatusDialog() {
        String[] statuses = {"IN_PROGRESS", "RESOLVED", "CLOSED"};
        String[] labels   = {"In Progress", "Resolved", "Closed"};
        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setItems(labels, (d, which) -> updateStatus(statuses[which]))
                .show();
    }

    private void updateStatus(String status) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        ApiClient.create(TechnicianApi.class).updateStatus(demandeId, body)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Demande> call, Response<Demande> r) {
                        if (r.isSuccessful()) runOnUiThread(() -> {
                            loadDemande();
                            loadTimeline();
                            Toast.makeText(DemandeDetailActivity.this,
                                    "Status updated", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override public void onFailure(Call<Demande> call, Throwable t) {}
                });
    }

    private void showActionDialog() {
        String[] actions = {
                "TECHNICIAN_GOING_TO_SITE", "TECHNICIAN_AT_SITE",
                "TECHNICIAN_GETTING_RESOURCES", "TECHNICIAN_FIXING_ISSUE",
                "ISSUE_RESOLVED", "WAITING_FOR_PARTS"
        };
        String[] labels = {
                "Going to site", "At site",
                "Getting resources", "Fixing issue",
                "Issue resolved", "Waiting for parts"
        };
        new AlertDialog.Builder(this)
                .setTitle("Log Action")
                .setItems(labels, (d, which) -> {
                    EditText et = new EditText(this);
                    et.setHint("Add a note (optional)");
                    new AlertDialog.Builder(this)
                            .setTitle(labels[which])
                            .setView(et)
                            .setPositiveButton("Log", (dd, ww) ->
                                    logAction(actions[which], et.getText().toString()))
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .show();
    }

    private void logAction(String action, String note) {
        Map<String, String> body = new HashMap<>();
        body.put("status", action);
        body.put("note", note);
        ApiClient.create(TechnicianApi.class).addAction(demandeId, body)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<DemandeAction> call, Response<DemandeAction> r) {
                        if (r.isSuccessful()) runOnUiThread(() -> {
                            loadTimeline();
                            Toast.makeText(DemandeDetailActivity.this,
                                    "Action logged", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override public void onFailure(Call<DemandeAction> call, Throwable t) {}
                });
    }


    private void takePhoto() {
        File photoFile = new File(getCacheDir() + "/photos",
                "photo_" + System.currentTimeMillis() + ".jpg");
        //noinspection ResultOfMethodCallIgnored
        photoFile.getParentFile().mkdirs();
        photoUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    private void uploadPhoto(Uri uri) {
        try {
            byte[] bytes = getContentResolver().openInputStream(uri).readAllBytes();
            RequestBody reqBody = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part
                    .createFormData("photo", "photo.jpg", reqBody);

            ApiClient.create(TechnicianApi.class)
                    .uploadPhoto(demandeId, part)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call,
                                               Response<Map<String, Object>> r) {
                            runOnUiThread(() -> Toast.makeText(
                                    DemandeDetailActivity.this,
                                    r.isSuccessful() ? "Photo uploaded" : "Upload failed",
                                    Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            runOnUiThread(() -> Toast.makeText(
                                    DemandeDetailActivity.this, "Upload failed",
                                    Toast.LENGTH_SHORT).show());
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error reading photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void openNavigation() {
        if (demande == null || demande.latitude == null) return;
        Uri gmmIntentUri = Uri.parse(
                "google.navigation:q=" + demande.latitude + "," + demande.longitude + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination="
                            + demande.latitude + "," + demande.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
