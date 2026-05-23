package com.example.bookease.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookease.R;
import com.example.bookease.models.Appointment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    List<Appointment> items;
    private OnCancelClickListener cancelListener;
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    public interface OnCancelClickListener {
        void onCancelClick(Appointment appointment);
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.cancelListener = listener;
    }

    public AppointmentAdapter(List<Appointment> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = items.get(pos);
        h.tvService.setText(a.service_name);
        h.tvBranch.setText(a.branch_name);
        h.tvAddress.setText(a.branch_address != null ? a.branch_address : "N/A");
        
        // Format date for better readability
        try {
            LocalDate date = LocalDate.parse(a.appointment_date, inputFormatter);
            h.tvDate.setText(date.format(outputFormatter));
        } catch (Exception e) {
            h.tvDate.setText(a.appointment_date);
        }

        h.tvTime.setText(a.appointment_time);

        if (a.notes != null && !a.notes.isEmpty()) {
            h.llNotes.setVisibility(View.VISIBLE);
            h.tvNotes.setText(a.notes);
        } else {
            h.llNotes.setVisibility(View.GONE);
        }

        if (a.status == null || a.status.isEmpty()) {
            h.tvStatus.setText("Unknown");
        } else {
            h.tvStatus.setText(a.status.substring(0, 1).toUpperCase() + a.status.substring(1));
        }

        int textColor, bgColor;
        String status = a.status != null ? a.status.toLowerCase() : "";
        switch (status) {
            case "confirmed":
                textColor = Color.parseColor("#2ECC71"); // Brighter green
                bgColor = Color.parseColor("#202ECC71");
                h.btnCancel.setVisibility(View.VISIBLE);
                break;
            case "cancelled":
                textColor = Color.parseColor("#FF5252"); // Brighter red
                bgColor = Color.parseColor("#20FF5252");
                h.btnCancel.setVisibility(View.GONE);
                break;
            default:
                textColor = Color.parseColor("#FFB74D"); // Brighter orange
                bgColor = Color.parseColor("#20FFB74D");
                h.btnCancel.setVisibility(View.GONE);
                break;
        }

        h.tvStatus.setTextColor(textColor);
        h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        h.btnCancel.setOnClickListener(v -> {
            if (cancelListener != null) cancelListener.onCancelClick(a);
        });

        // Snappier fade-in animation
        h.itemView.setAlpha(0f);
        h.itemView.animate().alpha(1f).setDuration(150).start();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvService, tvBranch, tvAddress, tvNotes, tvStatus, tvDate, tvTime;
        View llNotes, btnCancel;

        VH(View v) {
            super(v);
            tvService  = v.findViewById(R.id.tvService);
            tvBranch   = v.findViewById(R.id.tvBranch);
            tvAddress  = v.findViewById(R.id.tvAddress);
            tvNotes    = v.findViewById(R.id.tvNotes);
            tvStatus   = v.findViewById(R.id.tvStatus);
            tvDate     = v.findViewById(R.id.tvDate);
            tvTime     = v.findViewById(R.id.tvTime);
            llNotes    = v.findViewById(R.id.llNotes);
            btnCancel  = v.findViewById(R.id.btnCancel);
        }
    }
}
