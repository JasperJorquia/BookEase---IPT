package com.example.bookease.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.example.bookease.R;
import com.example.bookease.adapters.AppointmentAdapter;
import com.example.bookease.models.Appointment;
import com.example.bookease.models.MessageResponse;
import com.example.bookease.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class HistoryFragment extends Fragment {

    RecyclerView rvAppointments;
    View layoutEmpty;
    MaterialButton btnClearAll;
    MaterialButton btnAll, btnConfirmed, btnCancelled;
    SwipeRefreshLayout swipeRefresh;
    AppointmentAdapter adapter;
    List<Appointment> appointments = new ArrayList<>();
    String currentFilter = "all";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_history, container, false);

        rvAppointments = v.findViewById(R.id.rvAppointments);
        layoutEmpty    = v.findViewById(R.id.tvEmpty);
        btnClearAll    = v.findViewById(R.id.tvClearAll);
        btnAll         = v.findViewById(R.id.btnAll);
        btnConfirmed   = v.findViewById(R.id.btnConfirmed);
        btnCancelled   = v.findViewById(R.id.btnCancelled);
        swipeRefresh   = v.findViewById(R.id.swipeRefresh);

        adapter = new AppointmentAdapter(appointments);
        adapter.setOnCancelClickListener(this::confirmCancel);
        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppointments.setAdapter(adapter);

        setFilter("all");
        
        btnAll.setOnClickListener(v2       -> setFilter("all"));
        btnConfirmed.setOnClickListener(v2 -> setFilter("confirmed"));
        btnCancelled.setOnClickListener(v2 -> setFilter("cancelled"));

        btnClearAll.setOnClickListener(v2   -> confirmClearAll());

        swipeRefresh.setOnRefreshListener(this::loadAppointments);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

        return v;
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        loadAppointments();
    }

    private void updateFilterButtons() {
        if (!isAdded()) return;
        
        int activeBg      = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
        int inactiveBg    = ContextCompat.getColor(requireContext(), R.color.cardBackground);
        int activeText    = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveText  = ContextCompat.getColor(requireContext(), R.color.textSecondary);
        int strokeColor   = ContextCompat.getColor(requireContext(), R.color.divider);

        updateButtonStyle(btnAll, "all".equals(currentFilter), activeBg, inactiveBg, activeText, inactiveText, strokeColor);
        updateButtonStyle(btnConfirmed, "confirmed".equals(currentFilter), activeBg, inactiveBg, activeText, inactiveText, strokeColor);
        updateButtonStyle(btnCancelled, "cancelled".equals(currentFilter), activeBg, inactiveBg, activeText, inactiveText, strokeColor);
    }

    private void updateFilterCounts(List<Appointment> allAppointments) {
        int total = allAppointments.size();
        int confirmed = 0;
        int cancelled = 0;

        for (Appointment a : allAppointments) {
            if (a.status == null) continue;
            if ("confirmed".equalsIgnoreCase(a.status)) confirmed++;
            else if ("cancelled".equalsIgnoreCase(a.status)) cancelled++;
        }

        btnAll.setText("All (" + total + ")");
        btnConfirmed.setText("Confirmed (" + confirmed + ")");
        btnCancelled.setText("Cancelled (" + cancelled + ")");
    }

    private void updateButtonStyle(MaterialButton btn, boolean isActive, int activeBg, int inactiveBg, int activeText, int inactiveText, int strokeColor) {
        btn.setBackgroundTintList(ColorStateList.valueOf(isActive ? activeBg : inactiveBg));
        btn.setTextColor(isActive ? activeText : inactiveText);
        btn.setStrokeColor(ColorStateList.valueOf(isActive ? Color.TRANSPARENT : strokeColor));
        btn.setStrokeWidth(isActive ? 0 : (int)(1 * getResources().getDisplayMetrics().density));
    }

    private void loadAppointments() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getApiService().getAppointments("").enqueue(new Callback<List<Appointment>>() {
            @Override public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> r) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);

                appointments.clear();
                if (r.isSuccessful() && r.body() != null) {
                    List<Appointment> all = r.body();
                    updateFilterCounts(all);
                    
                    if ("all".equals(currentFilter)) {
                        appointments.addAll(all);
                    } else {
                        for (Appointment a : all) {
                            if (a.status != null && currentFilter.equalsIgnoreCase(a.status)) {
                                appointments.add(a);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                boolean empty = appointments.isEmpty();
                layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvAppointments.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
            @Override public void onFailure(Call<List<Appointment>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                boolean empty = appointments.isEmpty();
                layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvAppointments.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void confirmClearAll() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.DestructiveMaterialAlertDialog)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all appointment records?")
            .setPositiveButton("Clear All", (d, w) -> clearAll())
            .setNegativeButton("Cancel", null)
            .show();
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
        ApiClient.getApiService().cancelAppointment(a.id).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> r) {
                if (isAdded()) loadAppointments();
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) {}
        });
    }

    private void clearAll() {
        ApiClient.getApiService().clearAppointments().enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> r) {
                if (isAdded()) loadAppointments();
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) {}
        });
    }
}
