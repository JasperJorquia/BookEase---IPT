package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.bookease.R;
import com.example.bookease.models.MessageResponse;
import com.example.bookease.models.UpdateProfileRequest;
import com.example.bookease.models.User;
import com.example.bookease.network.ApiClient;
import com.example.bookease.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileBottomSheet extends BottomSheetDialogFragment {

    EditText etName, etLocation;
    TextView tvAvatarInitial;
    Button btnSave, btnCancel;
    ProgressBar progressBar;
    SessionManager session;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_profile, container, false);

        etName         = v.findViewById(R.id.etName);
        etLocation     = v.findViewById(R.id.etLocation);
        tvAvatarInitial = v.findViewById(R.id.tvAvatarInitial);
        btnSave        = v.findViewById(R.id.btnSave);
        btnCancel      = v.findViewById(R.id.btnCancelProfile);
        progressBar    = v.findViewById(R.id.progressBar);

        session = new SessionManager(requireContext());

        // Pre-fill with current data
        etName.setText(session.getName());
        etLocation.setText(session.getLocation());
        String name = session.getName();
        tvAvatarInitial.setText(name.isEmpty() ? "?" :
                String.valueOf(name.charAt(0)).toUpperCase());

        btnCancel.setOnClickListener(v2 -> dismiss());
        btnSave.setOnClickListener(v2 -> saveProfile());

        return v;
    }

    private void saveProfile() {
        String name     = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.name     = name;
        req.location = location;

        ApiClient.getApiService().updateProfile(req)
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call,
                                           Response<MessageResponse> r) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        if (r.isSuccessful() && isAdded()) {
                            session.updateName(name);
                            session.updateLocation(location);
                            Toast.makeText(requireContext(),
                                    "Profile updated!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(),
                                "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}