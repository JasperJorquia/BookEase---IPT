package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.bookease.R;
import com.example.bookease.models.*;
import com.example.bookease.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewAppointmentBottomSheet extends BottomSheetDialogFragment {

    View btnSelectService, btnSelectBranch;
    TextView tvSelectedService, tvSelectedBranch;
    TextView tvDate, tvTime;
    EditText etNotes;
    Button btnBook, btnCancel;
    ProgressBar progressBar;

    List<Service> services = new ArrayList<>();
    List<Branch> branches = new ArrayList<>();
    int selectedServiceIndex = -1;
    int selectedBranchIndex = -1;
    
    Runnable onBooked;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public void setOnBookedListener(Runnable r) { this.onBooked = r; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.bottom_sheet_new_appointment, container, false);

        btnSelectService  = v.findViewById(R.id.btnSelectService);
        btnSelectBranch   = v.findViewById(R.id.btnSelectBranch);
        tvSelectedService = v.findViewById(R.id.tvSelectedService);
        tvSelectedBranch  = v.findViewById(R.id.tvSelectedBranch);
        
        tvTime       = v.findViewById(R.id.tvTime);
        tvDate       = v.findViewById(R.id.tvDate);
        etNotes      = v.findViewById(R.id.etNotes);
        btnBook      = v.findViewById(R.id.btnBook);
        btnCancel    = v.findViewById(R.id.btnCancel);
        progressBar  = v.findViewById(R.id.progressBar);

        btnSelectService.setOnClickListener(v2 -> showServiceSelection());
        btnSelectBranch.setOnClickListener(v2 -> showBranchSelection());

        // Modern Date picker
        tvDate.setOnClickListener(v2 -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setTheme(R.style.CustomDatePickerTheme)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                tvDate.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });

        // Modern Time picker
        tvTime.setOnClickListener(v2 -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTheme(R.style.CustomTimePickerTheme)
                    .setTitleText("Select Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v3 -> {
                int h = timePicker.getHour();
                int m = timePicker.getMinute();
                String ampm = h < 12 ? "AM" : "PM";
                int displayHour = h % 12;
                if (displayHour == 0) displayHour = 12;
                tvTime.setText(String.format(Locale.getDefault(), "%d:%02d %s", displayHour, m, ampm));
            });
            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });

        btnCancel.setOnClickListener(v2 -> dismiss());
        btnBook.setOnClickListener(v2 -> bookAppointment());

        loadData();
        return v;
    }

    private void showServiceSelection() {
        if (services.isEmpty()) return;
        List<String> names = new ArrayList<>();
        for (Service s : services) names.add(s.name);

        SelectionBottomSheet sheet = SelectionBottomSheet.newInstance("Choose a service", names, null, selectedServiceIndex);
        sheet.setOnItemSelectedListener((index, text) -> {
            selectedServiceIndex = index;
            tvSelectedService.setText(text);
            tvSelectedService.setTextColor(getResources().getColor(R.color.white, null));
        });
        sheet.show(getChildFragmentManager(), "select_service");
    }

    private void showBranchSelection() {
        if (branches.isEmpty()) return;
        List<String> names = new ArrayList<>();
        List<String> addresses = new ArrayList<>();
        for (Branch b : branches) {
            names.add(b.name);
            addresses.add(b.location);
        }

        SelectionBottomSheet sheet = SelectionBottomSheet.newInstance("Choose a branch", names, addresses, selectedBranchIndex);
        sheet.setOnItemSelectedListener((index, text) -> {
            selectedBranchIndex = index;
            tvSelectedBranch.setText(text);
            tvSelectedBranch.setTextColor(getResources().getColor(R.color.white, null));
        });
        sheet.show(getChildFragmentManager(), "select_branch");
    }

    private void loadData() {
        ApiClient.getApiService().getServices().enqueue(new Callback<List<Service>>() {
            @Override public void onResponse(Call<List<Service>> call, Response<List<Service>> r) {
                if (r.isSuccessful() && r.body() != null && isAdded()) {
                    services.clear(); services.addAll(r.body());
                }
            }
            @Override public void onFailure(Call<List<Service>> call, Throwable t) {}
        });

        ApiClient.getApiService().getBranches("").enqueue(new Callback<List<Branch>>() {
            @Override public void onResponse(Call<List<Branch>> call, Response<List<Branch>> r) {
                if (r.isSuccessful() && r.body() != null && isAdded()) {
                    branches.clear(); branches.addAll(r.body());
                }
            }
            @Override public void onFailure(Call<List<Branch>> call, Throwable t) {}
        });
    }

    private void bookAppointment() {
        String date    = tvDate.getText().toString();
        String time    = tvTime.getText().toString();
        String notes   = etNotes.getText().toString().trim();

        if (selectedServiceIndex < 0) { Toast.makeText(requireContext(), "Please select a service", Toast.LENGTH_SHORT).show(); return; }
        if (selectedBranchIndex  < 0) { Toast.makeText(requireContext(), "Please select a branch",  Toast.LENGTH_SHORT).show(); return; }
        if (date.isEmpty()) { Toast.makeText(requireContext(), "Please select a date",     Toast.LENGTH_SHORT).show(); return; }
        if (time.isEmpty()) { Toast.makeText(requireContext(), "Please select a time",     Toast.LENGTH_SHORT).show(); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnBook.setEnabled(false);

        AppointmentRequest req = new AppointmentRequest();
        req.service_id        = services.get(selectedServiceIndex).id;
        req.branch_id         = branches.get(selectedBranchIndex).id;
        req.appointment_date  = date;
        req.appointment_time  = time;
        req.notes             = notes;

        ApiClient.getApiService().createAppointment(req).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> r) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnBook.setEnabled(true);
                if (r.isSuccessful()) {
                    Toast.makeText(requireContext(), "Appointment booked!", Toast.LENGTH_SHORT).show();
                    if (onBooked != null) onBooked.run();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Booking failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnBook.setEnabled(true);
                Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
