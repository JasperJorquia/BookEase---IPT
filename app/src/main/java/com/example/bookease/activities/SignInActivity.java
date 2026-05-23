package com.example.bookease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookease.R;
import com.example.bookease.activities.CreateAccountActivity;
import com.example.bookease.activities.MainActivity;
import com.example.bookease.models.LoginRequest;
import com.example.bookease.models.LoginResponse;
import com.example.bookease.network.ApiClient;
import com.example.bookease.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnSignIn;
    TextView tvSignUp, tvForgot;
    ImageView ivTogglePass;
    ProgressBar progressBar;
    boolean passVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-redirect if already logged in
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            ApiClient.setToken(session.getToken());
            startActivity(new Intent(this, MainActivity.class));
            finish(); return;
        }

        setContentView(R.layout.activity_sign_in);

        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnSignIn    = findViewById(R.id.btnSignIn);
        tvSignUp     = findViewById(R.id.tvSignUp);
        tvForgot     = findViewById(R.id.tvForgot);
        ivTogglePass = findViewById(R.id.ivTogglePass);
        progressBar  = findViewById(R.id.progressBar);

        ivTogglePass.setOnClickListener(v -> {
            passVisible = !passVisible;
            etPassword.setTransformationMethod(passVisible
                    ? null : PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().length());
            ivTogglePass.setImageResource(passVisible
                    ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        });

        btnSignIn.setOnClickListener(v -> attemptLogin(session));

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccountActivity.class)));
    }

    private void attemptLogin(SessionManager session) {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        LoginRequest req = new LoginRequest();
        req.email = email; req.password = password;

        ApiClient.getApiService().login(req).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    session.saveSession(body.token, body.id, body.name, body.email, body.location);
                    ApiClient.setToken(body.token);
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);
                Toast.makeText(SignInActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
