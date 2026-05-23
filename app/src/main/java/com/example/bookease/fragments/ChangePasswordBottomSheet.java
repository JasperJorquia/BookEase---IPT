package com.example.bookease.fragments;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.bookease.R;
import com.example.bookease.models.ChangePasswordRequest;
import com.example.bookease.models.MessageResponse;
import com.example.bookease.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    EditText etOldPassword, etNewPassword, etConfirmPassword;
    ImageView ivToggleOld, ivToggleNew;
    Button btnUpdate, btnCancel;
    ProgressBar progressBar;

    boolean oldVisible = false;
    boolean newVisible = false;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_change_password, container, false);

        etOldPassword     = v.findViewById(R.id.etOldPassword);
        etNewPassword     = v.findViewById(R.id.etNewPassword);
        etConfirmPassword = v.findViewById(R.id.etConfirmPassword);
        ivToggleOld       = v.findViewById(R.id.ivToggleOld);
        ivToggleNew       = v.findViewById(R.id.ivToggleNew);
        btnUpdate         = v.findViewById(R.id.btnUpdate);
        btnCancel         = v.findViewById(R.id.btnCancelPw);
        progressBar       = v.findViewById(R.id.progressBar);

        ivToggleOld.setOnClickListener(v2 -> {
            oldVisible = !oldVisible;
            etOldPassword.setTransformationMethod(
                    oldVisible ? null : PasswordTransformationMethod.getInstance());
            etOldPassword.setSelection(etOldPassword.getText().length());
            ivToggleOld.setImageResource(oldVisible
                    ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        });

        ivToggleNew.setOnClickListener(v2 -> {
            newVisible = !newVisible;
            etNewPassword.setTransformationMethod(
                    newVisible ? null : PasswordTransformationMethod.getInstance());
            etNewPassword.setSelection(etNewPassword.getText().length());
            ivToggleNew.setImageResource(newVisible
                    ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        });

        btnCancel.setOnClickListener(v2 -> dismiss());
        btnUpdate.setOnClickListener(v2 -> updatePassword());

        return v;
    }

    private void updatePassword() {
        String oldPass     = etOldPassword.getText().toString().trim();
        String newPass     = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(requireContext(),
                    "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(requireContext(),
                    "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.old_password = oldPass;
        req.new_password = newPass;

        ApiClient.getApiService().changePassword(req)
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call,
                                           Response<MessageResponse> r) {
                        progressBar.setVisibility(View.GONE);
                        btnUpdate.setEnabled(true);
                        if (r.isSuccessful() && isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Incorrect current password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnUpdate.setEnabled(true);
                        Toast.makeText(requireContext(),
                                "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}