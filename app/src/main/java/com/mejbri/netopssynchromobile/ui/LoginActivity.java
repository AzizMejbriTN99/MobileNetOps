package com.mejbri.netopssynchromobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.mejbri.netopssynchromobile.MainActivity;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.*;
import com.mejbri.netopssynchromobile.network.*;
import com.mejbri.netopssynchromobile.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button   btnLogin;
    private ProgressBar progress;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        progress   = findViewById(R.id.progress);
        tvError    = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (user.isEmpty() || pass.isEmpty()) {
            tvError.setText("Please fill in all fields.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        btnLogin.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        ApiClient.create(TechnicianApi.class)
                .login(new LoginRequest(user, pass))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> r) {
                        runOnUiThread(() -> {
                            progress.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            if (r.isSuccessful() && r.body() != null) {
                                LoginResponse body = r.body();
                                if (!"ROLE_TECHNICIAN".equals(body.role)) {
                                    tvError.setText("Access denied. Technician accounts only.");
                                    tvError.setVisibility(View.VISIBLE);
                                    return;
                                }
                                SessionManager.save(LoginActivity.this,
                                        body.token, body.username, body.role);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                tvError.setText("Invalid credentials.");
                                tvError.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            progress.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            tvError.setText("Connection failed: " + t.getMessage());
                            tvError.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }
}
