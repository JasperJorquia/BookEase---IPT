package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bookease.R;
import com.example.bookease.activities.MainActivity;
import com.example.bookease.models.Stats;
import com.example.bookease.network.ApiClient;
import com.example.bookease.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    TextView tvGreeting, tvName, tvLocation;
    TextView tvCurrentCount, tvRecentCount, tvBranchCount;
    TextView tvCurrentStatus, tvRecentStatus;
    Button btnCreateAppt;
    TextView tvViewBranches;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_home, container, false);

        tvGreeting      = v.findViewById(R.id.tvGreeting);
        tvName          = v.findViewById(R.id.tvName);
        tvLocation      = v.findViewById(R.id.tvLocation);
        tvCurrentCount  = v.findViewById(R.id.tvCurrentCount);
        tvRecentCount   = v.findViewById(R.id.tvRecentCount);
        tvBranchCount   = v.findViewById(R.id.tvBranchCount);
        tvCurrentStatus = v.findViewById(R.id.tvCurrentStatus);
        tvRecentStatus  = v.findViewById(R.id.tvRecentStatus);
        btnCreateAppt   = v.findViewById(R.id.btnCreateAppt);
        tvViewBranches  = v.findViewById(R.id.tvViewBranches);

        SessionManager session = new SessionManager(requireContext());

        // Greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning," : hour < 17 ? "Good Afternoon," : "Good Evening,";
        tvGreeting.setText(greeting);
        tvName.setText(session.getName());
        tvLocation.setText(session.getLocation());

        // Navigate to booking
        btnCreateAppt.setOnClickListener(v2 ->
            ((MainActivity) requireActivity()).navigateTo(R.id.nav_booking));

        // View nearby branches bottom sheet
        tvViewBranches.setOnClickListener(v2 -> {
            NearbyBranchesBottomSheet sheet = new NearbyBranchesBottomSheet();
            sheet.show(getChildFragmentManager(), "nearby");
        });

        loadStats();
        return v;
    }

    private void loadStats() {
        ApiClient.getApiService().getStats().enqueue(new Callback<Stats>() {
            @Override public void onResponse(Call<Stats> call, Response<Stats> r) {
                if (r.isSuccessful() && r.body() != null && isAdded()) {
                    Stats s = r.body();
                    tvCurrentCount.setText(String.valueOf(s.current_appointments));
                    tvRecentCount.setText(String.valueOf(s.recent_appointments));
                    tvBranchCount.setText(String.valueOf(s.branches_available));

                    // Update status labels
                    if (s.current_appointments > 0) {
                        tvCurrentStatus.setText("Active");
                        tvCurrentStatus.setTextColor(getResources().getColor(R.color.colorSuccess, null));
                    } else {
                        tvCurrentStatus.setText("None");
                        tvCurrentStatus.setTextColor(getResources().getColor(R.color.textMuted, null));
                    }

                    if (s.recent_appointments > 0) {
                        tvRecentStatus.setText("Recorded");
                        tvRecentStatus.setTextColor(getResources().getColor(R.color.colorSuccess, null));
                    } else {
                        tvRecentStatus.setText("None");
                        tvRecentStatus.setTextColor(getResources().getColor(R.color.textMuted, null));
                    }
                }
            }
            @Override public void onFailure(Call<Stats> call, Throwable t) {}
        });
    }
}
