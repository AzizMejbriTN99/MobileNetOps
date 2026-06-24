package com.mejbri.netopssynchromobile.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.ProfileResponse;
import com.mejbri.netopssynchromobile.model.ProfileUpdateRequest;
import com.mejbri.netopssynchromobile.network.ApiClient;
import com.mejbri.netopssynchromobile.network.TechnicianApi;
import com.mejbri.netopssynchromobile.util.SessionManager;

import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFirstname;
    private EditText etLastname;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etCurrentPassword;
    private EditText etNewPassword;

    private ImageView ivAvatar;

    private ProgressBar progress;

    private Button btnSave;
    private Button btnDeleteAvatar;

    private TextView tvUsername;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;

                        selectedImageUri = uri;

                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(ivAvatar);

                        uploadAvatar(uri);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);

        ivAvatar = findViewById(R.id.ivAvatar);

        progress = findViewById(R.id.progress);

        btnSave = findViewById(R.id.btnSave);
        btnDeleteAvatar = findViewById(R.id.btnDeleteAvatar);

        tvUsername = findViewById(R.id.tvUsername);

        ivAvatar.setOnClickListener(v ->
                imagePicker.launch("image/*"));

        btnDeleteAvatar.setOnClickListener(v ->
                deleteAvatar());

        btnSave.setOnClickListener(v ->
                saveProfile());

        loadProfile();
    }

    private void loadProfile() {

        progress.setVisibility(ProgressBar.VISIBLE);

        ApiClient.create(TechnicianApi.class)
                .getProfile()
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ProfileResponse> call,
                            Response<ProfileResponse> response) {

                        progress.setVisibility(ProgressBar.GONE);

                        if (!response.isSuccessful()
                                || response.body() == null)
                            return;

                        ProfileResponse p = response.body();

                        tvUsername.setText(p.username);

                        etFirstname.setText(p.firstname);
                        etLastname.setText(p.lastname);
                        etEmail.setText(p.email);
                        etPhone.setText(p.phone);

                        SessionManager.updateName(
                                ProfileActivity.this,
                                p.firstname,
                                p.lastname
                        );

                        if (p.hasAvatar) {
                            loadAvatar();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ProfileResponse> call,
                            Throwable t) {

                        progress.setVisibility(ProgressBar.GONE);

                        Toast.makeText(
                                ProfileActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void loadAvatar() {

        ApiClient.create(TechnicianApi.class)
                .getAvatar()
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        try {

                            if (!response.isSuccessful()
                                    || response.body() == null)
                                return;

                            byte[] bytes =
                                    response.body().bytes();

                            Glide.with(ProfileActivity.this)
                                    .load(bytes)
                                    .circleCrop()
                                    .into(ivAvatar);

                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {
                    }
                });
    }

    private void saveProfile() {

        ProfileUpdateRequest req =
                new ProfileUpdateRequest();

        req.firstname =
                etFirstname.getText().toString().trim();

        req.lastname =
                etLastname.getText().toString().trim();

        req.email =
                etEmail.getText().toString().trim();

        req.phone =
                etPhone.getText().toString().trim();

        req.currentPassword =
                etCurrentPassword.getText().toString();

        req.newPassword =
                etNewPassword.getText().toString();

        progress.setVisibility(ProgressBar.VISIBLE);

        ApiClient.create(TechnicianApi.class)
                .updateProfile(req)
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ProfileResponse> call,
                            Response<ProfileResponse> response) {

                        progress.setVisibility(ProgressBar.GONE);

                        if (!response.isSuccessful()) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Update failed",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        SessionManager.updateName(
                                ProfileActivity.this,
                                req.firstname,
                                req.lastname
                        );

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile updated",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<ProfileResponse> call,
                            Throwable t) {

                        progress.setVisibility(ProgressBar.GONE);

                        Toast.makeText(
                                ProfileActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void uploadAvatar(Uri uri) {

        try {

            InputStream stream =
                    getContentResolver().openInputStream(uri);

            byte[] bytes =
                    stream.readAllBytes();

            RequestBody requestBody =
                    RequestBody.create(
                            bytes,
                            okhttp3.MediaType.parse("image/jpeg"));

            MultipartBody.Part file =
                    MultipartBody.Part.createFormData(
                            "file",
                            "avatar.jpg",
                            requestBody);

            ApiClient.create(TechnicianApi.class)
                    .uploadAvatar(file)
                    .enqueue(new Callback<>() {

                        @Override
                        public void onResponse(
                                Call<ProfileResponse> call,
                                Response<ProfileResponse> response) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Avatar updated",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onFailure(
                                Call<ProfileResponse> call,
                                Throwable t) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void deleteAvatar() {

        ApiClient.create(TechnicianApi.class)
                .deleteAvatar()
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ProfileResponse> call,
                            Response<ProfileResponse> response) {

                        ivAvatar.setImageResource(
                                R.drawable.ic_profile
                        );

                        Toast.makeText(
                                ProfileActivity.this,
                                "Avatar removed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<ProfileResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                ProfileActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}