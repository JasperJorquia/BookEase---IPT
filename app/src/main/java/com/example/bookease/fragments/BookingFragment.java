package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bookease.R;
import com.example.bookease.adapters.AppointmentAdapter;
import com.example.bookease.models.Appointment;
import com.example.bookease.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {

    com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd;
    RecyclerView rvUpcoming;
    TextView tvSeeAll;
    SwipeRefreshLayout swipeRefresh;
    AppointmentAdapter adapter;
    List<Appointment> appointments = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_booking, container, false);

        fabAdd       = v.findViewById(R.id.fabAdd);
        rvUpcoming   = v.findViewById(R.id.rvUpcoming);
        tvSeeAll     = v.findViewById(R.id.tvSeeAll);
        swipeRefresh = v.findViewById(R.id.swipeRefresh);

        adapter = new AppointmentAdapter(appointments);
        adapter.setOnCancelClickListener(this::confirmCancel);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUpcoming.setAdapter(adapter);

        fabAdd.setOnClickListener(v2 -> showNewAppointmentSheet());
        tvSeeAll.setOnClickListener(v2 ->
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HistoryFragment())
                .addToBackStack(null).commit());

        swipeRefresh.setOnRefreshListener(this::loadUpcoming);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary, null));

        loadUpcoming();
        return v;
    }

    private void showNewAppointmentSheet() {
        NewAppointmentBottomSheet sheet = new NewAppointmentBottomSheet();
        sheet.setOnBookedListener(() -> loadUpcoming());
        sheet.show(getChildFragmentManager(), "new_appt");
    }

    private void confirmCancel(Appointment a) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.DestructiveMaterialAlertDialog)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (d, w) -> cancelAppointment(a))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(Appointment a) {
        ApiClient.getApiService().cancelAppointment(a.id).enqueue(new Callback<com.example.bookease.models.MessageResponse>() {
            @Override public void onResponse(Call<com.example.bookease.models.MessageResponse> call, Response<com.example.bookease.models.MessageResponse> r) {
                if (isAdded()) loadUpcoming();
            }
            @Override public void onFailure(Call<com.example.bookease.models.MessageResponse> call, Throwable t) {}
        });
    }

    private void loadUpcoming() {
        ApiClient.getApiService().getAppointments("confirmed").enqueue(new Callback<List<Appointment>>() {
            @Override public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> r) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);

                if (r.isSuccessful() && r.body() != null) {
                    appointments.clear();
                    // Show only first 2 for preview
                    List<Appointment> all = r.body();
                    appointments.addAll(all.subList(0, Math.min(2, all.size())));
                    adapter.notifyDataSetChanged();
                }
                
                // Show empty state if no appointments
                View emptyState = getView() != null ? getView().findViewById(R.id.llEmptyUpcoming) : null;
                if (emptyState != null) {
                    emptyState.setVisibility(appointments.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onFailure(Call<List<Appointment>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
            }
        });
    }
}
