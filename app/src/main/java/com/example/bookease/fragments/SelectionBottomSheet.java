package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookease.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;

public class SelectionBottomSheet extends BottomSheetDialogFragment {

    public interface OnItemSelectedListener {
        void onItemSelected(int index, String text);
    }

    private String title;
    private List<String> items;
    private List<String> subItems;
    private int selectedIndex;
    private OnItemSelectedListener listener;

    public static SelectionBottomSheet newInstance(String title, List<String> items, List<String> subItems, int selectedIndex) {
        SelectionBottomSheet fragment = new SelectionBottomSheet();
        fragment.title = title;
        fragment.items = items;
        fragment.subItems = subItems;
        fragment.selectedIndex = selectedIndex;
        return fragment;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_selection, container, false);
        ((TextView) v.findViewById(R.id.tvTitle)).setText(title);
        v.findViewById(R.id.ivClose).setOnClickListener(v1 -> dismiss());

        RecyclerView rv = v.findViewById(R.id.rvSelection);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new Adapter());

        return v;
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selection, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String item = items.get(position);
            holder.tvMain.setText(item);
            
            if (subItems != null && position < subItems.size()) {
                holder.tvSub.setText(subItems.get(position));
                holder.tvSub.setVisibility(View.VISIBLE);
            } else {
                holder.tvSub.setVisibility(View.GONE);
            }

            boolean isSelected = position == selectedIndex;
            holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            holder.tvMain.setTextColor(getResources().getColor(isSelected ? R.color.colorPrimary : R.color.textPrimary, null));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemSelected(position, item);
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class VH extends RecyclerView.ViewHolder {
        TextView tvMain, tvSub;
        ImageView ivCheck;
        VH(View v) {
            super(v);
            tvMain = v.findViewById(R.id.tvMain);
            tvSub = v.findViewById(R.id.tvSub);
            ivCheck = v.findViewById(R.id.ivCheck);
        }
    }
}
