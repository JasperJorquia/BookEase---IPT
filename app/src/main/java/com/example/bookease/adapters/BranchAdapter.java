package com.example.bookease.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.example.bookease.R;
import com.example.bookease.models.Branch;
import java.util.List;

public class BranchAdapter extends ArrayAdapter<Branch> {

    public BranchAdapter(Context ctx, List<Branch> items) {
        super(ctx, 0, items);
    }

    @Override
    public View getView(int pos, View convert, ViewGroup parent) {
        if (convert == null)
            convert = LayoutInflater.from(getContext())
                .inflate(R.layout.item_branch, parent, false);
        Branch b = getItem(pos);
        ((TextView) convert.findViewById(R.id.tvBranchName)).setText(b.name);
        ((TextView) convert.findViewById(R.id.tvBranchAddress)).setText(b.address);
        return convert;
    }
}
