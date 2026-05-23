package com.example.bookease.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.bookease.R;
import com.example.bookease.adapters.BranchAdapter;
import com.example.bookease.models.Branch;
import com.example.bookease.network.ApiClient;
import com.example.bookease.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class NearbyBranchesBottomSheet extends BottomSheetDialogFragment {

    ListView lvBranches;
    ImageView ivClose;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.bottom_sheet_nearby_branches, container, false);

        lvBranches = v.findViewById(R.id.lvBranches);
        ivClose    = v.findViewById(R.id.ivClose);

        ivClose.setOnClickListener(v2 -> dismiss());

        SessionManager session = new SessionManager(requireContext());
        ApiClient.getApiService().getBranches(session.getLocation())
            .enqueue(new Callback<List<Branch>>() {
                @Override public void onResponse(Call<List<Branch>> call, Response<List<Branch>> r) {
                    if (r.isSuccessful() && r.body() != null && isAdded()) {
                        lvBranches.setAdapter(new BranchAdapter(requireContext(), r.body()));
                    }
                }
                @Override public void onFailure(Call<List<Branch>> call, Throwable t) {}
            });

        return v;
    }
}
