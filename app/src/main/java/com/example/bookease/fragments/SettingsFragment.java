package com.example.bookease.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bookease.R;
import com.example.bookease.activities.SignInActivity;
import com.example.bookease.network.ApiClient;
import com.example.bookease.utils.SessionManager;

public class SettingsFragment extends Fragment {

    LinearLayout llProfile, llPassword, llNotification, llRateReview, llHelp, llLogout;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_settings, container, false);

        llProfile      = v.findViewById(R.id.llProfile);
        llPassword     = v.findViewById(R.id.llPassword);
        llNotification = v.findViewById(R.id.llNotification);
        llRateReview   = v.findViewById(R.id.llRateReview);
        llHelp         = v.findViewById(R.id.llHelp);
        llLogout       = v.findViewById(R.id.llLogout);

        llProfile.setOnClickListener(v2 -> {
            ProfileBottomSheet sheet = new ProfileBottomSheet();
            sheet.show(getChildFragmentManager(), "profile");
        });

        llPassword.setOnClickListener(v2 -> {
            ChangePasswordBottomSheet sheet = new ChangePasswordBottomSheet();
            sheet.show(getChildFragmentManager(), "password");
        });

        llNotification.setOnClickListener(v2 ->
            Toast.makeText(requireContext(), "Notifications coming soon", Toast.LENGTH_SHORT).show());

        llRateReview.setOnClickListener(v2 ->
            Toast.makeText(requireContext(), "Thank you for your support!", Toast.LENGTH_SHORT).show());

        llHelp.setOnClickListener(v2 ->
            Toast.makeText(requireContext(), "Help center coming soon", Toast.LENGTH_SHORT).show());

        llLogout.setOnClickListener(v2 -> confirmLogout());

        return v;
    }

    private void confirmLogout() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.DestructiveMaterialAlertDialog)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out", (d, w) -> {
                new SessionManager(requireContext()).logout();
                ApiClient.setToken(""); // Clear the cached token
                Intent intent = new Intent(requireActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
