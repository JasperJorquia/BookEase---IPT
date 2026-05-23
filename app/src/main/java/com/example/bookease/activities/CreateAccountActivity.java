package com.example.bookease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookease.R;
import com.example.bookease.models.MessageResponse;
import com.example.bookease.models.RegisterRequest;
import com.example.bookease.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPass;
    Button btnCreate;
    TextView tvSignIn;
    ImageView ivTogglePass, ivToggleConfirmPass;
    ProgressBar progressBar;
    boolean passVisible = false;
    boolean confirmPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etName        = findViewById(R.id.etName);
        etEmail       = findViewById(R.id.etEmail);
        etPassword    = findViewById(R.id.etPassword);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        btnCreate     = findViewById(R.id.btnCreate);
        tvSignIn      = findViewById(R.id.tvSignIn);
        ivTogglePass  = findViewById(R.id.ivTogglePass);
        ivToggleConfirmPass = findViewById(R.id.ivToggleConfirmPass);
        progressBar   = findViewById(R.id.progressBar);

        ivTogglePass.setOnClickListener(v -> {
            passVisible = !passVisible;
            togglePasswordVisibility(etPassword, ivTogglePass, passVisible);
        });

        ivToggleConfirmPass.setOnClickListener(v -> {
            confirmPassVisible = !confirmPassVisible;
            togglePasswordVisibility(etConfirmPass, ivToggleConfirmPass, confirmPassVisible);
        });

        btnCreate.setOnClickListener(v -> attemptRegister());
        tvSignIn.setOnClickListener(v -> finish());
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView, boolean visible) {
        if (visible) {
            editText.setTransformationMethod(null);
            imageView.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye);
        }
        editText.setSelection(editText.getText().length());
    }

    private void attemptRegister() {
        String name    = etName.getText().toString().trim();
        String email   = etEmail.getText().toString().trim();
        String pass    = etPassword.getText().toString().trim();
        String confirm = etConfirmPass.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        RegisterRequest req = new RegisterRequest();
        req.name = name; req.email = email; req.password = pass;

        ApiClient.getApiService().register(req).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> r) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                if (r.isSuccessful()) {
                    Toast.makeText(CreateAccountActivity.this,
                            "Account created! Please sign in.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateAccountActivity.this, SignInActivity.class));
                    finish();
                } else {
                    Toast.makeText(CreateAccountActivity.this,
                            "Email already registered", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(CreateAccountActivity.this,
                        "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
